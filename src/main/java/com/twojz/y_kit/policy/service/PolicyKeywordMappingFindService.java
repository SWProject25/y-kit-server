package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.repository.PolicyKeywordMappingRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyKeywordMappingFindService {
    private final PolicyKeywordMappingRepository policyKeywordMappingRepository;

    public List<PolicyKeywordMapping> findByPolicy(PolicyEntity policy) {
        return policyKeywordMappingRepository.findByPolicy(policy);
    }

    public Map<Long, List<PolicyKeywordMapping>> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyKeywordMappingRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.groupingBy(mapping -> mapping.getPolicy().getId()));
    }
}
