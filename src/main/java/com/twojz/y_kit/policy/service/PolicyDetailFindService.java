package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyDetailRepository;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyDetailFindService {
    private final PolicyDetailRepository policyDetailRepository;

    public PolicyDetailEntity findNullableByPolicy(PolicyEntity policy) {
        return policyDetailRepository.findByPolicy(policy).orElse(null);
    }

    public Map<Long, PolicyDetailEntity> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        return policyDetailRepository.findByPolicyIn(policies).stream()
                .collect(Collectors.toMap(detail -> detail.getPolicy().getId(), Function.identity()));
    }
}
