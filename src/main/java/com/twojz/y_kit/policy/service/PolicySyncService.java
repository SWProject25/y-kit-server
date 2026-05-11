package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import com.twojz.y_kit.policy.domain.dto.PolicyDetailDto;
import com.twojz.y_kit.policy.domain.dto.PolicyQualificationDto;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.repository.RegionRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySyncService {
    private static final int BATCH_SIZE = 100;
    private final String reg = "\\s*(,|및)\\s*";

    private final YouthPolicyClient youthPolicyClient;
    private final PolicyEntityFindService policyEntityFindService;
    private final PolicyEntityCommandService policyEntityCommandService;
    private final PolicyDetailFindService policyDetailFindService;
    private final PolicyDetailCommandService policyDetailCommandService;
    private final PolicyApplicationFindService policyApplicationFindService;
    private final PolicyApplicationCommandService policyApplicationCommandService;
    private final PolicyQualificationFindService policyQualificationFindService;
    private final PolicyQualificationCommandService policyQualificationCommandService;
    private final PolicyDocumentFindService policyDocumentFindService;
    private final PolicyDocumentCommandService policyDocumentCommandService;
    private final PolicyCategoryFindService policyCategoryFindService;
    private final PolicyCategoryCommandService policyCategoryCommandService;
    private final PolicyCategoryMappingFindService policyCategoryMappingFindService;
    private final PolicyCategoryMappingCommandService policyCategoryMappingCommandService;
    private final PolicyKeywordFindService policyKeywordFindService;
    private final PolicyKeywordCommandService policyKeywordCommandService;
    private final PolicyKeywordMappingFindService policyKeywordMappingFindService;
    private final PolicyKeywordMappingCommandService policyKeywordMappingCommandService;
    private final PolicyRegionFindService policyRegionFindService;
    private final PolicyRegionCommandService policyRegionCommandService;
    private final RegionRepository regionRepository;
    private final PolicyMapper mapper;
    private final PolicyAiAnalysisService policyAiAnalysisService;

    /**
     * 정책 동기화
     */
    public void syncAllPolicies() {
        try {
            List<YouthPolicy> policies = youthPolicyClient.fetchAllPolicies()
                    .timeout(Duration.ofMinutes(5))
                    .block();

            if (policies == null || policies.isEmpty()) {
                return;
            }

            int totalSize = policies.size();
            List<PolicyEntity> newPolicies = new ArrayList<>();

            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalSize);
                List<YouthPolicy> batch = policies.subList(i, end);

                try {
                    newPolicies.addAll(processBatch(batch));
                } catch (Exception e) {
                    log.error("배치 처리 실패 (index: {}-{})", i, end, e);
                }
            }

            deactivateMissingPolicies(policies);

            if (!newPolicies.isEmpty()) {
                processAiAnalysisForNewPolicies(newPolicies);
            }

        } catch (Exception e) {
            log.error("정책 동기화 중 오류 발생", e);
            throw new RuntimeException("정책 동기화 실패", e);
        }
    }

    /**
     * 새 정책 추적이 가능한 배치 처리
     */
    @Transactional
    public List<PolicyEntity> processBatch(List<YouthPolicy> batch) {
        List<PolicyEntity> newPolicies = new ArrayList<>();

        Set<String> policyNos = batch.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        Map<String, PolicyEntity> existingPolicies = policyEntityFindService
                .findAllByPolicyNos(policyNos)
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyNo, p -> p));

        List<PolicyEntity> policyEntities = new ArrayList<>(existingPolicies.values());

        Map<Long, List<PolicyCategoryMapping>> categoryMappingsMap =
                policyCategoryMappingFindService.findMapByPolicies(policyEntities);

        Map<Long, List<PolicyKeywordMapping>> keywordMappingsMap =
                policyKeywordMappingFindService.findMapByPolicies(policyEntities);

        Map<Long, List<PolicyRegion>> regionMappingsMap =
                policyRegionFindService.findMapByPolicies(policyEntities);

        Map<String, PolicyCategoryEntity> categoryCache = new HashMap<>();
        Map<String, PolicyKeywordEntity> keywordCache = new HashMap<>();

        for (YouthPolicy apiPolicy : batch) {
            try {
                PolicyEntity policy = existingPolicies.get(apiPolicy.getPlcyNo());
                boolean isNew = policy == null;

                if (isNew) {
                    policy = policyEntityCommandService.save(PolicyEntity.builder()
                            .policyNo(apiPolicy.getPlcyNo())
                            .isActive(true)
                            .build());
                    existingPolicies.put(policy.getPolicyNo(), policy);
                    newPolicies.add(policy);
                }

                policy.activate();

                updatePolicy(apiPolicy, policy,
                        categoryMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        keywordMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        regionMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        categoryCache, keywordCache);

            } catch (Exception e) {
                log.error("정책 저장 실패: {}", apiPolicy.getPlcyNo(), e);
            }
        }

        return newPolicies;
    }

    /**
     * 새 정책들에 대해 AI 분석 처리
     */
    private void processAiAnalysisForNewPolicies(List<PolicyEntity> newPolicies) {
        for (PolicyEntity policy : newPolicies) {
            try {
                policyAiAnalysisService.processAiAnalysis(policy);
            } catch (Exception e) {
                log.error("AI 분석 실패 - policyNo: {}", policy.getPolicyNo(), e);
            }
        }
    }

    private boolean updatePolicy(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyCategoryMapping> existingCategoryMappings,
            List<PolicyKeywordMapping> existingKeywordMappings,
            List<PolicyRegion> existingRegionMappings,
            Map<String, PolicyCategoryEntity> categoryCache,
            Map<String, PolicyKeywordEntity> keywordCache) {

        PolicyDetailDto detailReq = mapper.toDetailRequest(apiPolicy);
        PolicyApplicationDto appReq = mapper.toApplicationRequest(apiPolicy);
        PolicyQualificationDto qualReq = mapper.toQualificationRequest(apiPolicy);

        boolean detailChanged = updateOrCreateDetail(policy, detailReq);
        boolean appChanged = updateOrCreateApplication(policy, appReq);
        boolean qualChanged = updateOrCreateQualification(policy, qualReq);
        boolean docChanged = updateOrCreateDocument(policy, apiPolicy.getSbmsnDcmntCn());

        boolean categoryChanged = updateCategoryMappings(apiPolicy, policy, existingCategoryMappings, categoryCache);
        boolean keywordChanged = updateKeywordMappings(apiPolicy, policy, existingKeywordMappings, keywordCache);
        boolean regionChanged = updateRegionMappings(apiPolicy, policy, existingRegionMappings);

        return detailChanged || appChanged || qualChanged || docChanged ||
                categoryChanged || keywordChanged || regionChanged;
    }

    private boolean updateOrCreateDetail(PolicyEntity policy, PolicyDetailDto dto) {
        PolicyDetailEntity detail = policyDetailFindService.findNullableByPolicy(policy);
        boolean isNew = detail == null;
        if (isNew) {
            detail = PolicyDetailEntity.builder().policy(policy).build();
        }

        detail.updateFromApi(dto);
        if (isNew) {
            policyDetailCommandService.save(detail);
        }
        return isNew;
    }

    private boolean updateOrCreateApplication(PolicyEntity policy, PolicyApplicationDto dto) {
        PolicyApplicationEntity application = policyApplicationFindService.findNullableByPolicy(policy);
        boolean isNew = application == null;
        if (isNew) {
            application = PolicyApplicationEntity.builder().policy(policy).build();
        }

        application.updateFromApi(dto);
        if (isNew) {
            policyApplicationCommandService.save(application);
        }
        return isNew;
    }

    private boolean updateOrCreateQualification(PolicyEntity policy, PolicyQualificationDto dto) {
        PolicyQualificationEntity qualification = policyQualificationFindService.findNullableByPolicy(policy);
        boolean isNew = qualification == null;
        if (isNew) {
            qualification = PolicyQualificationEntity.builder().policy(policy).build();
        }

        qualification.updateFromApi(dto);
        if (isNew) {
            policyQualificationCommandService.save(qualification);
        }
        return isNew;
    }

    private boolean updateOrCreateDocument(PolicyEntity policy, String original) {
        if (isEmptyDocument(original)) return false;
        PolicyDocumentEntity document = policyDocumentFindService.findNullableByPolicy(policy);
        boolean isNew = document == null;
        if (isNew) {
            document = PolicyDocumentEntity.builder().policy(policy).build();
        }

        if (isNew || !java.util.Objects.equals(document.getDocumentsOriginal(), original)) {
            document.updateOriginal(original);
            DocumentParsed parsed = DocumentPreprocessor.parse(original);
            document.updateParsed(parsed);
            policyDocumentCommandService.save(document);
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
            Map<String, PolicyCategoryEntity> categoryCache) {

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

        policyCategoryMappingCommandService.deleteAll(existingMappings);
        policyCategoryMappingCommandService.saveAll(newMappings);

        return true;
    }

    private boolean updateKeywordMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyKeywordMapping> existingMappings,
            Map<String, PolicyKeywordEntity> keywordCache) {

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
            policyKeywordMappingCommandService.deleteAll(toDelete);
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
            policyKeywordMappingCommandService.saveAll(toAdd);
        }

        return true;
    }

    private PolicyCategoryEntity findOrCreateCategoryWithCache(
            String name,
            int level,
            PolicyCategoryEntity parent,
            Map<String, PolicyCategoryEntity> cache
    ) {
        String cacheKey = name + "_" + level;
        return cache.computeIfAbsent(cacheKey, key ->
                policyCategoryFindService.findByNameAndLevel(name, level)
                        .orElseGet(() -> policyCategoryCommandService.save(
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
            Map<String, PolicyKeywordEntity> cache
    ) {
        return cache.computeIfAbsent(keywordText, key ->
                policyKeywordFindService.findByKeyword(keywordText)
                        .orElseGet(() -> policyKeywordCommandService.save(
                                PolicyKeywordEntity.builder()
                                        .keyword(keywordText)
                                        .usageCount(0)
                                        .build()
                        ))
        );
    }

    private boolean updateRegionMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            List<PolicyRegion> existingMappings) {

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
            policyRegionCommandService.deleteAll(toDelete);
        }

        if (!toAddIds.isEmpty()) {
            List<PolicyRegion> toAdd = newRegions.stream()
                    .filter(region -> toAddIds.contains(region.getCode()))
                    .map(region -> PolicyRegion.builder()
                            .policy(policy)
                            .region(region)
                            .build())
                    .toList();
            policyRegionCommandService.saveAll(toAdd);
        }

        return true;
    }

    @Transactional
    public int deactivateMissingPolicies(List<YouthPolicy> apiPolicies) {
        Set<String> apiPolicyNos = apiPolicies.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        List<PolicyEntity> activePolicies = policyEntityFindService.findAllActiveEntities();

        List<PolicyEntity> toDeactivate = activePolicies.stream()
                .filter(policy -> !apiPolicyNos.contains(policy.getPolicyNo()))
                .peek(PolicyEntity::deactivate)
                .toList();

        if (!toDeactivate.isEmpty()) {
            policyEntityCommandService.saveAll(toDeactivate);
        }

        return toDeactivate.size();
    }
}
