package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.repository.PolicyQualificationRepository;
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
public class PolicyQualificationFindService {
    private final PolicyQualificationRepository policyQualificationRepository;

    public PolicyQualificationEntity findNullableByPolicy(PolicyEntity policy) {
        return policyQualificationRepository.findByPolicy(policy).orElse(null);
    }

    public Map<Long, PolicyQualificationEntity> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        return policyQualificationRepository.findByPolicyIn(policies).stream()
                .collect(Collectors.toMap(qualification -> qualification.getPolicy().getId(), Function.identity()));
    }
}
