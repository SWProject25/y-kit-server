package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.entity.*;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import com.twojz.y_kit.policy.domain.dto.PolicyDetailDto;
import com.twojz.y_kit.policy.domain.dto.PolicyQualificationDto;
import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import com.twojz.y_kit.policy.repository.*;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySyncService {
    private static final int BATCH_SIZE = 100;
    private final String reg = "\\s*(,|및)\\s*";

    private final YouthPolicyClient youthPolicyClient;
    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyApplicationRepository policyApplicationRepository;
    private final PolicyQualificationRepository policyQualificationRepository;
    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final PolicyCategoryMappingRepository policyCategoryMappingRepository;
    private final PolicyKeywordRepository policyKeywordRepository;
    private final PolicyKeywordMappingRepository policyKeywordMappingRepository;
    private final PolicyRegionRepository policyRegionRepository;
    private final RegionRepository regionRepository;
    private final PolicyMapper mapper;
    private final PolicyAiAnalysisService policyAiAnalysisService;

    private static class SyncStatistics {
        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int failed = 0;
        int deactivated = 0;

        int categoryCreated = 0;
        int categoryDeleted = 0;
        int keywordCreated = 0;
        int keywordDeleted = 0;
        int regionCreated = 0;
        int regionDeleted = 0;

        @Override
        public String toString() {
            return String.format(
                    "정책: 생성=%d, 수정=%d, 변경없음=%d, 실패=%d, 비활성화=%d | " +
                            "카테고리: +%d/-%d, 키워드: +%d/-%d, 지역: +%d/-%d",
                    created, updated, unchanged, failed, deactivated,
                    categoryCreated, categoryDeleted,
                    keywordCreated, keywordDeleted,
                    regionCreated, regionDeleted
            );
        }
    }

    /**
     * 배치 처리 결과 (통계 + 새 정책 목록)
     */
    private static class SyncResult {
        SyncStatistics stats;
        List<PolicyEntity> newPolicies;

        SyncResult(SyncStatistics stats, List<PolicyEntity> newPolicies) {
            this.stats = stats;
            this.newPolicies = newPolicies;
        }
    }

    /**
     * 기본 정책 동기화 (AI 분석 없음)
     */
    public void syncAllPolicies() {
        log.info("정책 동기화 시작");
        long startTime = System.currentTimeMillis();

        try {
            List<YouthPolicy> policies = youthPolicyClient.fetchAllPolicies()
                    .timeout(Duration.ofMinutes(5))
                    .block();

            if (policies == null || policies.isEmpty()) {
                log.warn("동기화할 정책이 없습니다.");
                return;
            }

            log.info("이 {}개 정책 동기화 시작", policies.size());

            int totalSize = policies.size();
            SyncStatistics totalStats = new SyncStatistics();

            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalSize);
                List<YouthPolicy> batch = policies.subList(i, end);

                try {
                    SyncStatistics batchStats = processBatch(batch);

                    totalStats.created += batchStats.created;
                    totalStats.updated += batchStats.updated;
                    totalStats.unchanged += batchStats.unchanged;
                    totalStats.failed += batchStats.failed;
                    totalStats.categoryCreated += batchStats.categoryCreated;
                    totalStats.categoryDeleted += batchStats.categoryDeleted;
                    totalStats.keywordCreated += batchStats.keywordCreated;
                    totalStats.keywordDeleted += batchStats.keywordDeleted;
                    totalStats.regionCreated += batchStats.regionCreated;
                    totalStats.regionDeleted += batchStats.regionDeleted;

                    if ((i / BATCH_SIZE) % 10 == 0) {
                        int processed = totalStats.created + totalStats.updated + totalStats.unchanged + totalStats.failed;
                        log.info("진행 중: {}/{} - 생성: {}, 수정: {}, 변경없음: {}",
                                processed, totalSize, totalStats.created, totalStats.updated, totalStats.unchanged);
                    }
                } catch (Exception e) {
                    log.error("배치 처리 실패 (index: {}-{})", i, end, e);
                    totalStats.failed += batch.size();
                }
            }

            // API에 없는 정책 비활성화
            totalStats.deactivated = deactivateMissingPolicies(policies);

            long duration = System.currentTimeMillis() - startTime;
            log.info("=".repeat(80));
            log.info("정책 동기화 완료 - 소요시간: {}초", duration / 1000);
            log.info(totalStats.toString());
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("정책 동기화 중 오류 발생", e);
            throw new RuntimeException("정책 동기화 실패", e);
        }
    }

    /**
     * AI 분석 포함한 정책 동기화 (스케줄러용)
     */
    public void syncAllPoliciesWithAi() {
        log.info("정책 동기화 시작 (AI 분석 포함)");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 기본 정책 동기화
            List<YouthPolicy> policies = youthPolicyClient.fetchAllPolicies()
                    .timeout(Duration.ofMinutes(5))
                    .block();

            if (policies == null || policies.isEmpty()) {
                log.warn("동기화할 정책이 없습니다.");
                return;
            }

            log.info("이 {}개 정책 동기화 시작", policies.size());

            int totalSize = policies.size();
            SyncStatistics totalStats = new SyncStatistics();
            List<PolicyEntity> newPolicies = new ArrayList<>();

            // 2. 배치별 정책 동기화 (새 정책 추적)
            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalSize);
                List<YouthPolicy> batch = policies.subList(i, end);

                try {
                    SyncResult result = processBatchWithTracking(batch);

                    totalStats.created += result.stats.created;
                    totalStats.updated += result.stats.updated;
                    totalStats.unchanged += result.stats.unchanged;
                    totalStats.failed += result.stats.failed;
                    totalStats.categoryCreated += result.stats.categoryCreated;
                    totalStats.categoryDeleted += result.stats.categoryDeleted;
                    totalStats.keywordCreated += result.stats.keywordCreated;
                    totalStats.keywordDeleted += result.stats.keywordDeleted;
                    totalStats.regionCreated += result.stats.regionCreated;
                    totalStats.regionDeleted += result.stats.regionDeleted;

                    newPolicies.addAll(result.newPolicies);

                    if ((i / BATCH_SIZE) % 10 == 0) {
                        int processed = totalStats.created + totalStats.updated + totalStats.unchanged + totalStats.failed;
                        log.info("진행 중: {}/{} - 생성: {}, 수정: {}, 변경없음: {}",
                                processed, totalSize, totalStats.created, totalStats.updated, totalStats.unchanged);
                    }
                } catch (Exception e) {
                    log.error("배치 처리 실패 (index: {}-{})", i, end, e);
                    totalStats.failed += batch.size();
                }
            }

            // 3. API에 없는 정책 비활성화
            totalStats.deactivated = deactivateMissingPolicies(policies);

            long syncDuration = System.currentTimeMillis() - startTime;
            log.info("=".repeat(80));
            log.info("정책 동기화 완료 - 소요시간: {}초", syncDuration / 1000);
            log.info(totalStats.toString());
            log.info("=".repeat(80));

            // 4. 새로 생성된 정책에 대해 AI 분석 실행
            if (!newPolicies.isEmpty()) {
                log.info("새로 생성된 정책 {}개에 대해 AI 분석 시작", newPolicies.size());
                processAiAnalysisForNewPolicies(newPolicies);
            }

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("전체 작업 완료 - 총 소요시간: {}초", totalDuration / 1000);

        } catch (Exception e) {
            log.error("정책 동기화 중 오류 발생", e);
            throw new RuntimeException("정책 동기화 실패", e);
        }
    }

    @Transactional
    public SyncStatistics processBatch(List<YouthPolicy> batch) {
        SyncStatistics stats = new SyncStatistics();

        // 1. 기존 정책 조회 (한 번에)
        Set<String> policyNos = batch.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        Map<String, PolicyEntity> existingPolicies = policyRepository
                .findAllByPolicyNoIn(policyNos)
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyNo, p -> p));

        // 2. 배치의 모든 매핑을 한 번에 조회 (N+1 방지)
        List<PolicyEntity> policyEntities = new ArrayList<>(existingPolicies.values());

        Map<Long, List<PolicyCategoryMapping>> categoryMappingsMap =
                policyCategoryMappingRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        Map<Long, List<PolicyKeywordMapping>> keywordMappingsMap =
                policyKeywordMappingRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        Map<Long, List<PolicyRegion>> regionMappingsMap =
                policyRegionRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        // 3. 카테고리/키워드 캐싱
        Map<String, PolicyCategoryEntity> categoryCache = new HashMap<>();
        Map<String, PolicyKeywordEntity> keywordCache = new HashMap<>();

        for (YouthPolicy apiPolicy : batch) {
            try {
                PolicyEntity policy = existingPolicies.get(apiPolicy.getPlcyNo());
                boolean isNew = policy == null;

                if (isNew) {
                    policy = policyRepository.save(PolicyEntity.builder()
                            .policyNo(apiPolicy.getPlcyNo())
                            .isActive(true)
                            .build());
                    existingPolicies.put(policy.getPolicyNo(), policy);
                    stats.created++;
                }

                policy.activate();

                boolean hasChanges = updatePolicy(apiPolicy, policy,
                        categoryMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        keywordMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        regionMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        categoryCache, keywordCache, stats);

                if (!isNew) {
                    if (hasChanges) {
                        stats.updated++;
                    } else {
                        stats.unchanged++;
                    }
                }

            } catch (Exception e) {
                log.error("정책 저장 실패: {}", apiPolicy.getPlcyNo(), e);
                stats.failed++;
            }
        }

        return stats;
    }

    /**
     * 새 정책 추적이 가능한 배치 처리
     */
    @Transactional
    public SyncResult processBatchWithTracking(List<YouthPolicy> batch) {
        SyncStatistics stats = new SyncStatistics();
        List<PolicyEntity> newPolicies = new ArrayList<>();

        Set<String> policyNos = batch.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        Map<String, PolicyEntity> existingPolicies = policyRepository
                .findAllByPolicyNoIn(policyNos)
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyNo, p -> p));

        List<PolicyEntity> policyEntities = new ArrayList<>(existingPolicies.values());

        Map<Long, List<PolicyCategoryMapping>> categoryMappingsMap =
                policyCategoryMappingRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        Map<Long, List<PolicyKeywordMapping>> keywordMappingsMap =
                policyKeywordMappingRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        Map<Long, List<PolicyRegion>> regionMappingsMap =
                policyRegionRepository.findByPolicyIn(policyEntities)
                        .stream()
                        .collect(Collectors.groupingBy(m -> m.getPolicy().getId()));

        Map<String, PolicyCategoryEntity> categoryCache = new HashMap<>();
        Map<String, PolicyKeywordEntity> keywordCache = new HashMap<>();

        for (YouthPolicy apiPolicy : batch) {
            try {
                PolicyEntity policy = existingPolicies.get(apiPolicy.getPlcyNo());
                boolean isNew = policy == null;

                if (isNew) {
                    policy = policyRepository.save(PolicyEntity.builder()
                            .policyNo(apiPolicy.getPlcyNo())
                            .isActive(true)
                            .build());
                    existingPolicies.put(policy.getPolicyNo(), policy);
                    newPolicies.add(policy);
                    stats.created++;
                }

                policy.activate();

                boolean hasChanges = updatePolicy(apiPolicy, policy,
                        categoryMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        keywordMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        regionMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        categoryCache, keywordCache, stats);

                if (!isNew) {
                    if (hasChanges) {
                        stats.updated++;
                    } else {
                        stats.unchanged++;
                    }
                }

            } catch (Exception e) {
                log.error("정책 저장 실패: {}", apiPolicy.getPlcyNo(), e);
                stats.failed++;
            }
        }

        return new SyncResult(stats, newPolicies);
    }

    /**
     * 새 정책들에 대해 AI 분석 처리
     */
    private void processAiAnalysisForNewPolicies(List<PolicyEntity> newPolicies) {
        int successCount = 0;
        int failCount = 0;

        for (PolicyEntity policy : newPolicies) {
            try {
                policyAiAnalysisService.processAiAnalysis(policy);
                successCount++;

                if (successCount % 10 == 0) {
                    log.info("AI 분석 진행 중: {}/{}", successCount, newPolicies.size());
                }
            } catch (Exception e) {
                log.error("AI 분석 실패 - policyNo: {}", policy.getPolicyNo(), e);
                failCount++;
            }
        }

        log.info("AI 분석 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }

    private boolean updatePolicy(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyCategoryMapping> existingCategoryMappings,
            List<PolicyKeywordMapping> existingKeywordMappings,
            List<PolicyRegion> existingRegionMappings,
            Map<String, PolicyCategoryEntity> categoryCache,
            Map<String, PolicyKeywordEntity> keywordCache,
            SyncStatistics stats) {

        PolicyDetailDto detailReq = mapper.toDetailRequest(apiPolicy);
        PolicyApplicationDto appReq = mapper.toApplicationRequest(apiPolicy);
        PolicyQualificationDto qualReq = mapper.toQualificationRequest(apiPolicy);

        boolean detailChanged = updateOrCreateDetail(policy, detailReq);
        boolean appChanged = updateOrCreateApplication(policy, appReq);
        boolean qualChanged = updateOrCreateQualification(policy, qualReq);
        boolean docChanged = updateOrCreateDocument(policy, apiPolicy.getSbmsnDcmntCn());

        boolean categoryChanged = updateCategoryMappings(apiPolicy, policy, existingCategoryMappings, categoryCache, stats);
        boolean keywordChanged = updateKeywordMappings(apiPolicy, policy, existingKeywordMappings, keywordCache, stats);
        boolean regionChanged = updateRegionMappings(apiPolicy, policy, existingRegionMappings, stats);

        return detailChanged || appChanged || qualChanged || docChanged ||
                categoryChanged || keywordChanged || regionChanged;
    }

    private boolean updateOrCreateDetail(PolicyEntity policy, PolicyDetailDto dto) {
        PolicyDetailEntity detail = policyDetailRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyDetailEntity.builder().policy(policy).build());

        boolean isNew = detail.getId() == null;
        detail.updateFromApi(dto);

        if (isNew) {
            policyDetailRepository.save(detail);
        }

        return isNew;
    }

    private boolean updateOrCreateApplication(PolicyEntity policy, PolicyApplicationDto dto) {
        PolicyApplicationEntity app = policyApplicationRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyApplicationEntity.builder().policy(policy).build());

        boolean isNew = app.getId() == null;
        app.updateFromApi(dto);

        if (isNew) {
            policyApplicationRepository.save(app);
        }

        return isNew;
    }

    private boolean updateOrCreateQualification(PolicyEntity policy, PolicyQualificationDto dto) {
        PolicyQualificationEntity qual = policyQualificationRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyQualificationEntity.builder().policy(policy).build());

        boolean isNew = qual.getId() == null;
        qual.updateFromApi(dto);

        if (isNew) {
            policyQualificationRepository.save(qual);
        }

        return isNew;
    }

    private boolean updateOrCreateDocument(PolicyEntity policy, String original) {
        if (isEmptyDocument(original)) return false;

        PolicyDocumentEntity doc = policyDocumentRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyDocumentEntity.builder()
                        .policy(policy)
                        .build());

        boolean isNew = doc.getId() == null;

        if (isNew || !Objects.equals(doc.getDocumentsOriginal(), original)) {
            doc.updateOriginal(original);
            DocumentParsed parsed = DocumentPreprocessor.parse(original);
            doc.updateParsed(parsed);

            policyDocumentRepository.save(doc);
            return true;
        }

        return false;
    }

    private boolean isEmptyDocument(String original) {
        return original == null ||
                original.trim().isEmpty() ||
                original.equals("-") ||
                original.equals("없음");
    }

    private boolean updateCategoryMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyCategoryMapping> existingMappings,
            Map<String, PolicyCategoryEntity> categoryCache,
            SyncStatistics stats) {

        Function<String, Set<String>> parse = txt -> Arrays.stream(txt.split(reg))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PolicyCategoryMapping> newMappings = new ArrayList<>();

        if (StringUtils.hasText(apiPolicy.getLclsfNm())) {
            parse.apply(apiPolicy.getLclsfNm()).forEach(name -> {
                PolicyCategoryEntity main = findOrCreateCategoryWithCache(name, 1, null, categoryCache);
                newMappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(main)
                        .build());
            });
        }

        if (StringUtils.hasText(apiPolicy.getMclsfNm())) {
            PolicyCategoryEntity mainParent = newMappings.isEmpty() ? null : newMappings.getFirst().getCategory();

            parse.apply(apiPolicy.getMclsfNm()).forEach(name -> {
                PolicyCategoryEntity sub = findOrCreateCategoryWithCache(name, 2, mainParent, categoryCache);
                newMappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(sub)
                        .build());
            });
        }

        List<Long> existingCategoryIds = existingMappings.stream()
                .map(m -> m.getCategory().getId())
                .toList();
        List<Long> newCategoryIds = newMappings.stream()
                .map(m -> m.getCategory().getId())
                .toList();

        if (existingCategoryIds.equals(newCategoryIds)) {
            return false;
        }

        if (!existingMappings.isEmpty()) {
            policyCategoryMappingRepository.deleteAll(existingMappings);
            stats.categoryDeleted += existingMappings.size();
        }

        if (!newMappings.isEmpty()) {
            policyCategoryMappingRepository.saveAll(newMappings);
            stats.categoryCreated += newMappings.size();
        }

        return true;
    }

    private boolean updateKeywordMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyKeywordMapping> existingMappings,
            Map<String, PolicyKeywordEntity> keywordCache,
            SyncStatistics stats) {

        Map<String, PolicyKeywordMapping> existingKeywordMap = existingMappings.stream()
                .collect(Collectors.toMap(
                        m -> m.getKeyword().getKeyword(),
                        m -> m
                ));

        final Set<String> newKeywords;
        if (StringUtils.hasText(apiPolicy.getPlcyKywdNm())) {
            newKeywords = Arrays.stream(apiPolicy.getPlcyKywdNm().split(","))
                    .map(String::trim)
                    .filter(kw -> !kw.isEmpty())
                    .collect(Collectors.toSet());
        } else {
            newKeywords = Collections.emptySet();
        }

        List<PolicyKeywordMapping> toDelete = existingMappings.stream()
                .filter(m -> !newKeywords.contains(m.getKeyword().getKeyword()))
                .toList();

        Set<String> toAddKeywords = new HashSet<>(newKeywords);
        toAddKeywords.removeAll(existingKeywordMap.keySet());

        if (toDelete.isEmpty() && toAddKeywords.isEmpty()) {
            return false;
        }

        if (!toDelete.isEmpty()) {
            toDelete.forEach(m -> m.getKeyword().decreaseUsageCount());
            policyKeywordMappingRepository.deleteAll(toDelete);
            stats.keywordDeleted += toDelete.size();
        }

        if (!toAddKeywords.isEmpty()) {
            List<PolicyKeywordMapping> toAdd = toAddKeywords.stream()
                    .map(kw -> {
                        PolicyKeywordEntity keyword = findOrCreateKeywordWithCache(kw, keywordCache);
                        keyword.increaseUsageCount();
                        return PolicyKeywordMapping.builder()
                                .policy(policy)
                                .keyword(keyword)
                                .build();
                    })
                    .toList();
            policyKeywordMappingRepository.saveAll(toAdd);
            stats.keywordCreated += toAdd.size();
        }

        return true;
    }

    private boolean updateRegionMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyRegion> existingMappings,
            SyncStatistics stats) {

        Set<String> existingRegionIds = existingMappings.stream()
                .map(pr -> pr.getRegion().getCode())
                .collect(Collectors.toSet());

        Set<String> newRegionCodes = new HashSet<>();
        if (StringUtils.hasText(apiPolicy.getZipCd())) {
            newRegionCodes = Arrays.stream(apiPolicy.getZipCd().split("[,;]"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }

        List<Region> newRegions = regionRepository.findAllByCodeIn(new ArrayList<>(newRegionCodes));
        Set<String> newRegionIds = newRegions.stream()
                .map(Region::getCode)
                .collect(Collectors.toSet());

        List<PolicyRegion> toDelete = existingMappings.stream()
                .filter(pr -> !newRegionIds.contains(pr.getRegion().getCode()))
                .toList();

        Set<String> toAddIds = new HashSet<>(newRegionIds);
        toAddIds.removeAll(existingRegionIds);

        if (toDelete.isEmpty() && toAddIds.isEmpty()) {
            return false;
        }

        if (!toDelete.isEmpty()) {
            policyRegionRepository.deleteAll(toDelete);
            stats.regionDeleted += toDelete.size();
        }

        if (!toAddIds.isEmpty()) {
            List<PolicyRegion> toAdd = newRegions.stream()
                    .filter(region -> toAddIds.contains(region.getCode()))
                    .map(region -> PolicyRegion.builder()
                            .policy(policy)
                            .region(region)
                            .build())
                    .toList();
            policyRegionRepository.saveAll(toAdd);
            stats.regionCreated += toAdd.size();
        }

        return true;
    }

    private PolicyCategoryEntity findOrCreateCategoryWithCache(
            String name,
            int level,
            PolicyCategoryEntity parent,
            Map<String, PolicyCategoryEntity> cache) {

        String cacheKey = name + "_" + level;
        return cache.computeIfAbsent(cacheKey, k ->
                policyCategoryRepository.findByNameAndLevel(name, level)
                        .orElseGet(() -> policyCategoryRepository.save(
                                PolicyCategoryEntity.builder()
                                        .name(name)
                                        .level(level)
                                        .parent(parent)
                                        .isActive(true)
                                        .build()
                        ))
        );
    }

    private PolicyKeywordEntity findOrCreateKeywordWithCache(
            String keywordText,
            Map<String, PolicyKeywordEntity> cache) {

        return cache.computeIfAbsent(keywordText, k ->
                policyKeywordRepository.findByKeyword(keywordText)
                        .orElseGet(() -> policyKeywordRepository.save(
                                PolicyKeywordEntity.builder()
                                        .keyword(keywordText)
                                        .usageCount(0)
                                        .build()
                        ))
        );
    }

    @Transactional
    public int deactivateMissingPolicies(List<YouthPolicy> apiPolicies) {
        Set<String> apiPolicyNos = apiPolicies.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        List<PolicyEntity> activePolicies = policyRepository.findAllByIsActiveTrue();

        List<PolicyEntity> toDeactivate = activePolicies.stream()
                .filter(policy -> !apiPolicyNos.contains(policy.getPolicyNo()))
                .peek(PolicyEntity::deactivate)
                .toList();

        if (!toDeactivate.isEmpty()) {
            policyRepository.saveAll(toDeactivate);
            log.info("API에 없는 정책 {}개 비활성화 완료", toDeactivate.size());
        }

        return toDeactivate.size();
    }
}