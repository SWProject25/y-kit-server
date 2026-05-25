package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.client.YouthPolicyClient;
import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.service.persistence.PolicyApplicationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyCategoryPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDetailPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDocumentPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyEntityPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyKeywordPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyQualificationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyRegionPersistenceService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicySyncService {
    private static final int BATCH_SIZE = 100;

    private final YouthPolicyClient youthPolicyClient;
    private final PolicyEntityPersistenceService policyEntityPersistenceService;
    private final PolicyDetailPersistenceService policyDetailPersistenceService;
    private final PolicyApplicationPersistenceService policyApplicationPersistenceService;
    private final PolicyQualificationPersistenceService policyQualificationPersistenceService;
    private final PolicyDocumentPersistenceService policyDocumentPersistenceService;
    private final PolicyCategoryPersistenceService policyCategoryPersistenceService;
    private final PolicyKeywordPersistenceService policyKeywordPersistenceService;
    private final PolicyRegionPersistenceService policyRegionPersistenceService;
    private final PolicySectionSyncService policySectionSyncService;
    private final PolicyMappingSyncService policyMappingSyncService;
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

            // AI 분석 호출은 동기화 성능과 외부 API 의존성을 분리하기 위해 임시 비활성화합니다.
            // if (!newPolicies.isEmpty()) {
            //     processAiAnalysisForNewPolicies(newPolicies);
            // }

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

        Map<String, PolicyEntity> existingPolicies = policyEntityPersistenceService
                .findAllByPolicyNos(policyNos)
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyNo, p -> p));

        List<PolicyEntity> policyEntities = new ArrayList<>(existingPolicies.values());

        Map<Long, List<PolicyCategoryMapping>> categoryMappingsMap =
                policyCategoryPersistenceService.findMappingMapByPolicies(policyEntities);

        Map<Long, List<PolicyKeywordMapping>> keywordMappingsMap =
                policyKeywordPersistenceService.findMappingMapByPolicies(policyEntities);

        Map<Long, List<PolicyRegion>> regionMappingsMap =
                policyRegionPersistenceService.findMapByPolicies(policyEntities);

        Map<Long, PolicyDetailEntity> detailMap =
                policyDetailPersistenceService.findMapByPolicies(policyEntities);

        Map<Long, PolicyApplicationEntity> applicationMap =
                policyApplicationPersistenceService.findMapByPolicies(policyEntities);

        Map<Long, PolicyQualificationEntity> qualificationMap =
                policyQualificationPersistenceService.findMapByPolicies(policyEntities);

        Map<Long, PolicyDocumentEntity> documentMap =
                policyDocumentPersistenceService.findMapByPolicies(policyEntities);

        PolicyMappingSyncService.MappingSyncContext mappingContext = policyMappingSyncService.createContext();

        for (YouthPolicy apiPolicy : batch) {
            try {
                PolicyEntity policy = existingPolicies.get(apiPolicy.getPlcyNo());
                boolean isNew = policy == null;

                if (isNew) {
                    policy = policyEntityPersistenceService.save(PolicyEntity.builder()
                            .policyNo(apiPolicy.getPlcyNo())
                            .isActive(true)
                            .build());
                    existingPolicies.put(policy.getPolicyNo(), policy);
                    newPolicies.add(policy);
                }

                policy.activate();

                ExistingPolicyRelations relations = new ExistingPolicyRelations(
                        detailMap.get(policy.getId()),
                        applicationMap.get(policy.getId()),
                        qualificationMap.get(policy.getId()),
                        documentMap.get(policy.getId()),
                        categoryMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        keywordMappingsMap.getOrDefault(policy.getId(), Collections.emptyList()),
                        regionMappingsMap.getOrDefault(policy.getId(), Collections.emptyList())
                );

                updatePolicy(apiPolicy, policy, relations, mappingContext);

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
            ExistingPolicyRelations relations,
            PolicyMappingSyncService.MappingSyncContext mappingContext
    ) {
        boolean sectionChanged = policySectionSyncService.syncSections(
                apiPolicy,
                policy,
                relations.detail(),
                relations.application(),
                relations.qualification(),
                relations.document()
        );

        boolean mappingChanged = policyMappingSyncService.syncMappings(
                apiPolicy,
                policy,
                relations.categoryMappings(),
                relations.keywordMappings(),
                relations.regionMappings(),
                mappingContext
        );

        return sectionChanged || mappingChanged;
    }

    @Transactional
    public int deactivateMissingPolicies(List<YouthPolicy> apiPolicies) {
        Set<String> apiPolicyNos = apiPolicies.stream()
                .map(YouthPolicy::getPlcyNo)
                .collect(Collectors.toSet());

        List<PolicyEntity> activePolicies = policyEntityPersistenceService.findAllActiveEntities();

        List<PolicyEntity> toDeactivate = activePolicies.stream()
                .filter(policy -> !apiPolicyNos.contains(policy.getPolicyNo()))
                .peek(PolicyEntity::deactivate)
                .toList();

        if (!toDeactivate.isEmpty()) {
            policyEntityPersistenceService.saveAll(toDeactivate);
        }

        return toDeactivate.size();
    }

    private record ExistingPolicyRelations(
            PolicyDetailEntity detail,
            PolicyApplicationEntity application,
            PolicyQualificationEntity qualification,
            PolicyDocumentEntity document,
            List<PolicyCategoryMapping> categoryMappings,
            List<PolicyKeywordMapping> keywordMappings,
            List<PolicyRegion> regionMappings
    ) {}
}
