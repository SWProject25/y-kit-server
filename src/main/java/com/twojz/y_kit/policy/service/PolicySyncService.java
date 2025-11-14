package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.entity.*;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import com.twojz.y_kit.policy.domain.dto.PolicyDetailDto;
import com.twojz.y_kit.policy.domain.dto.PolicyQualificationDto;
import com.twojz.y_kit.policy.repository.*;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public void syncAllPolicies() {
        log.info("정책 동기화 시작");

        try {
            List<YouthPolicy> policies = youthPolicyClient.fetchAllPolicies().block();

            if (policies == null || policies.isEmpty()) {
                log.warn("동기화할 정책이 없습니다.");
                return;
            }

            log.info("총 {}개 정책 동기화 시작", policies.size());

            // 배치 단위로 처리
            int totalSize = policies.size();
            int success = 0, fail = 0;

            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalSize);
                List<YouthPolicy> batch = policies.subList(i, end);

                try {
                    int batchSuccess = processBatch(batch);
                    success += batchSuccess;
                    fail += (batch.size() - batchSuccess);

                    log.info("진행 중: {}/{} (배치: {}/{})",
                            success, totalSize, (i / BATCH_SIZE) + 1, (totalSize + BATCH_SIZE - 1) / BATCH_SIZE);
                } catch (Exception e) {
                    log.error("배치 처리 실패", e);
                    fail += batch.size();
                }
            }

            log.info("정책 동기화 완료 - 성공: {}, 실패: {}", success, fail);
        } catch (Exception e) {
            log.error("정책 동기화 중 오류 발생", e);
            throw new RuntimeException("정책 동기화 실패", e);
        }
    }

    @Transactional
    public int processBatch(List<YouthPolicy> batch) {
        // 1. 기존 정책 조회 (한 번에)
        Set<String> policyNos = batch.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        Map<String, PolicyEntity> existingPolicies = policyRepository
                .findAllByPolicyNoIn(policyNos)
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyNo, p -> p));

        // 2. 카테고리/키워드 캐싱
        Map<String, PolicyCategoryEntity> categoryCache = new HashMap<>();
        Map<String, PolicyKeywordEntity> keywordCache = new HashMap<>();

        int success = 0;
        for (YouthPolicy apiPolicy : batch) {
            try {
                saveOrUpdatePolicy(apiPolicy, existingPolicies, categoryCache, keywordCache);
                success++;
            } catch (Exception e) {
                log.error("정책 저장 실패: {}", apiPolicy.getPlcyNo(), e);
            }
        }

        return success;
    }

    private void saveOrUpdatePolicy(
            YouthPolicy apiPolicy,
            Map<String, PolicyEntity> existingPolicies,
            Map<String, PolicyCategoryEntity> categoryCache,
            Map<String, PolicyKeywordEntity> keywordCache) {

        PolicyEntity policy = existingPolicies.computeIfAbsent(
                apiPolicy.getPlcyNo(),
                no -> policyRepository.save(PolicyEntity.builder()
                        .policyNo(no)
                        .isActive(true)
                        .build())
        );

        PolicyDetailDto detailReq = mapper.toDetailRequest(apiPolicy);
        PolicyApplicationDto appReq = mapper.toApplicationRequest(apiPolicy);
        PolicyQualificationDto qualReq = mapper.toQualificationRequest(apiPolicy);

        updateOrCreateDetail(policy, detailReq);
        updateOrCreateApplication(policy, appReq);
        updateOrCreateQualification(policy, qualReq);
        updateOrCreateDocument(policy, apiPolicy.getSbmsnDcmntCn());

        updateCategoryMappings(apiPolicy, policy, categoryCache);
        updateKeywordMappings(apiPolicy, policy, keywordCache);
        updateRegionMappings(apiPolicy, policy);
    }

    private void updateOrCreateDetail(PolicyEntity policy, PolicyDetailDto dto) {
        PolicyDetailEntity detail = policyDetailRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyDetailEntity.builder().policy(policy).build());

        detail.updateFromApi(dto);
        if (detail.getId() == null) {
            policyDetailRepository.save(detail);
        }
    }

    private void updateOrCreateApplication(PolicyEntity policy, PolicyApplicationDto dto) {
        PolicyApplicationEntity app = policyApplicationRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyApplicationEntity.builder().policy(policy).build());

        app.updateFromApi(dto);
        if (app.getId() == null) {
            policyApplicationRepository.save(app);
        }
    }

    private void updateOrCreateQualification(PolicyEntity policy, PolicyQualificationDto dto) {
        PolicyQualificationEntity qual = policyQualificationRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyQualificationEntity.builder().policy(policy).build());

        qual.updateFromApi(dto);
        if (qual.getId() == null) {
            policyQualificationRepository.save(qual);
        }
    }

    private void updateOrCreateDocument(PolicyEntity policy, String original) {
        if (isEmptyDocument(original)) return;

        PolicyDocumentEntity doc = policyDocumentRepository.findByPolicy(policy)
                .orElseGet(() -> PolicyDocumentEntity.builder()
                        .policy(policy)
                        .build());

        doc.updateOriginal(original);
        if (doc.getId() == null) {
            policyDocumentRepository.save(doc);
        }
    }

    private boolean isEmptyDocument(String original) {
        return original == null ||
                original.trim().isEmpty() ||
                original.equals("-") ||
                original.equals("없음");
    }

    private void updateCategoryMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            Map<String, PolicyCategoryEntity> categoryCache) {

        // 기존 매핑 삭제 (벌크)
        policyCategoryMappingRepository.deleteByPolicy(policy);

        Function<String, Set<String>> parse = txt -> Arrays.stream(txt.split(reg))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PolicyCategoryMapping> mappings = new ArrayList<>();

        if (StringUtils.hasText(apiPolicy.getLclsfNm())) {
            parse.apply(apiPolicy.getLclsfNm()).forEach(name -> {
                PolicyCategoryEntity main = findOrCreateCategoryWithCache(name, 1, null, categoryCache);
                mappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(main)
                        .build());
            });
        }

        if (StringUtils.hasText(apiPolicy.getMclsfNm())) {
            PolicyCategoryEntity mainParent = mappings.isEmpty() ? null : mappings.get(0).getCategory();

            parse.apply(apiPolicy.getMclsfNm()).forEach(name -> {
                PolicyCategoryEntity sub = findOrCreateCategoryWithCache(name, 2, mainParent, categoryCache);
                mappings.add(PolicyCategoryMapping.builder()
                        .policy(policy)
                        .category(sub)
                        .build());
            });
        }

        if (!mappings.isEmpty()) {
            policyCategoryMappingRepository.saveAll(mappings);
        }
    }

    private void updateKeywordMappings(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            Map<String, PolicyKeywordEntity> keywordCache) {

        // 기존 키워드 카운트 감소 및 삭제
        List<PolicyKeywordMapping> existing = policyKeywordMappingRepository.findByPolicyWithKeyword(policy);
        existing.forEach(m -> m.getKeyword().decreaseUsageCount());
        if (!existing.isEmpty()) {
            policyKeywordMappingRepository.deleteByPolicy(policy);
        }

        if (!StringUtils.hasText(apiPolicy.getPlcyKywdNm())) return;

        List<PolicyKeywordMapping> mappings = Arrays.stream(apiPolicy.getPlcyKywdNm().split(","))
                .map(String::trim)
                .filter(kw -> !kw.isEmpty())
                .map(kw -> {
                    PolicyKeywordEntity keyword = findOrCreateKeywordWithCache(kw, keywordCache);
                    keyword.increaseUsageCount();
                    return PolicyKeywordMapping.builder()
                            .policy(policy)
                            .keyword(keyword)
                            .build();
                })
                .toList();

        if (!mappings.isEmpty()) {
            policyKeywordMappingRepository.saveAll(mappings);
        }
    }

    private void updateRegionMappings(YouthPolicy apiPolicy, PolicyEntity policy) {
        // 기존 매핑 삭제 (벌크)
        policyRegionRepository.deleteByPolicy(policy);

        if (!StringUtils.hasText(apiPolicy.getZipCd())) return;

        List<String> codes = Arrays.stream(apiPolicy.getZipCd().split("[,;]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        // 한 번에 조회
        List<Region> regions = regionRepository.findAllByCodeIn(codes);

        List<PolicyRegion> mappings = regions.stream()
                .map(region -> PolicyRegion.builder()
                        .policy(policy)
                        .region(region)
                        .build())
                .toList();

        if (!mappings.isEmpty()) {
            policyRegionRepository.saveAll(mappings);
        }
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
}