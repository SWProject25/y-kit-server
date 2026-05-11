package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import com.twojz.y_kit.policy.repository.PolicyApplicationRepository;
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
public class PolicyApplicationFindService {
    private final PolicyApplicationRepository policyApplicationRepository;

    public PolicyApplicationEntity findNullableByPolicy(PolicyEntity policy) {
        return policyApplicationRepository.findByPolicy(policy).orElse(null);
    }

    public Map<Long, PolicyApplicationEntity> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyApplicationRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.toMap(application -> application.getPolicy().getId(), Function.identity()));
    }
}
