package com.twojz.y_kit.policy.service.persistence;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyDetailRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyDetailPersistenceService {

    private final PolicyDetailRepository policyDetailRepository;

    public PolicyDetailEntity findNullableByPolicy(PolicyEntity policy) {
        return policyDetailRepository.findByPolicy(policy).orElse(null);
    }

    public PolicyDetailEntity findByPolicyOrThrow(PolicyEntity policy) {
        return policyDetailRepository.findByPolicy(policy)
                .orElseThrow(() -> new IllegalArgumentException("정책 상세 정보를 찾을 수 없습니다. policyId=" + policy.getId()));
    }

    public Map<Long, PolicyDetailEntity> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyDetailRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.toMap(detail -> detail.getPolicy().getId(), Function.identity()));
    }

    @Transactional
    public PolicyDetailEntity save(PolicyDetailEntity detail) {
        return policyDetailRepository.save(detail);
    }
}
