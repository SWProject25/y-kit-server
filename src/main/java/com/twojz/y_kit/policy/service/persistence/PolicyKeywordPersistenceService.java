package com.twojz.y_kit.policy.service.persistence;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.repository.PolicyKeywordMappingRepository;
import com.twojz.y_kit.policy.repository.PolicyKeywordRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyKeywordPersistenceService {

    private final PolicyKeywordRepository policyKeywordRepository;
    private final PolicyKeywordMappingRepository policyKeywordMappingRepository;

    public Optional<PolicyKeywordEntity> findByKeyword(String keyword) {
        return policyKeywordRepository.findByKeyword(keyword);
    }

    public List<PolicyKeywordEntity> findTop50ByUsageCount() {
        return policyKeywordRepository.findTop50ByOrderByUsageCountDesc();
    }

    public List<PolicyKeywordMapping> findMappingsByPolicy(PolicyEntity policy) {
        return policyKeywordMappingRepository.findByPolicy(policy);
    }

    public Map<Long, List<PolicyKeywordMapping>> findMappingMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyKeywordMappingRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.groupingBy(mapping -> mapping.getPolicy().getId()));
    }

    @Transactional
    public PolicyKeywordEntity save(PolicyKeywordEntity keyword) {
        return policyKeywordRepository.save(keyword);
    }

    @Transactional
    public void deleteMappings(List<PolicyKeywordMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyKeywordMappingRepository.deleteAll(mappings);
        }
    }

    @Transactional
    public void saveMappings(List<PolicyKeywordMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyKeywordMappingRepository.saveAll(mappings);
        }
    }
}
