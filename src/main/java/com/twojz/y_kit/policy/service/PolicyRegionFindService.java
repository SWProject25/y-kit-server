package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.repository.PolicyRegionRepository;
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
public class PolicyRegionFindService {
    private final PolicyRegionRepository policyRegionRepository;

    public List<PolicyRegion> findByPolicy(PolicyEntity policy) {
        return policyRegionRepository.findByPolicy(policy);
    }

    public Map<Long, List<PolicyRegion>> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        return policyRegionRepository.findByPolicyIn(List.copyOf(policies)).stream()
                .collect(Collectors.groupingBy(region -> region.getPolicy().getId()));
    }
}
