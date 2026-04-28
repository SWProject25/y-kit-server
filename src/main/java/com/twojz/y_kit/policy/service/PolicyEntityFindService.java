package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyEntityFindService {
    private final PolicyRepository policyRepository;

    public PolicyEntity findById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));
    }

    public List<PolicyEntity> findByIds(List<Long> ids) {
        return policyRepository.findAllById(ids);
    }

    public List<PolicyEntity> findAllByPolicyNos(Collection<String> policyNos) {
        return policyRepository.findAllByPolicyNoIn(policyNos);
    }

    public List<PolicyEntity> findAllActiveEntities() {
        return policyRepository.findAllByIsActiveTrue();
    }

    public Page<PolicyEntity> findAllActive(Pageable pageable) {
        return policyRepository.findAllActive(pageable);
    }

    public Page<PolicyEntity> findRecommendedWithProfile(
            Integer age,
            String regionCode,
            EmploymentStatus employmentStatus,
            EducationLevel educationLevel,
            MajorField major,
            Pageable pageable
    ) {
        return policyRepository.findRecommendedWithProfile(age, regionCode, employmentStatus, educationLevel, major, pageable);
    }

    public Page<PolicyEntity> findPopularByBookmarkCount(Pageable pageable) {
        return policyRepository.findPopularByBookmarkCount(pageable);
    }

    public Page<PolicyEntity> findPopularByViewCount(Pageable pageable) {
        return policyRepository.findPopularByViewCount(pageable);
    }

    public Page<PolicyEntity> findDeadlineSoon(java.time.LocalDate today, Pageable pageable) {
        return policyRepository.findDeadlineSoon(today, pageable);
    }

    public Page<PolicyEntity> searchPolicies(
            List<Long> categoryIds,
            List<Long> keywordIds,
            List<String> keywords,
            Pageable pageable
    ) {
        return policyRepository.searchPolicies(categoryIds, keywordIds, keywords, pageable);
    }

    public List<PolicyEntity> findSimilarByCategory(Long policyId, int limit) {
        return policyRepository.findSimilarByCategory(policyId, limit);
    }

    public Page<PolicyEntity> findPoliciesWithoutAiAnalysis(Pageable pageable) {
        return policyRepository.findAllByAiAnalysisIsNull(pageable);
    }

    public long countPoliciesWithoutAiAnalysis() {
        return policyRepository.countByAiAnalysisIsNull();
    }

    public long countPoliciesWithAiAnalysis() {
        return policyRepository.countByAiAnalysisIsNotNull();
    }
}
