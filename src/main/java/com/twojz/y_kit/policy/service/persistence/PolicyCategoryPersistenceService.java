package com.twojz.y_kit.policy.service.persistence;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyCategoryMappingRepository;
import com.twojz.y_kit.policy.repository.PolicyCategoryRepository;
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
public class PolicyCategoryPersistenceService {

    private final PolicyCategoryRepository policyCategoryRepository;
    private final PolicyCategoryMappingRepository policyCategoryMappingRepository;

    public Optional<PolicyCategoryEntity> findByNameAndLevel(String name, Integer level) {
        return policyCategoryRepository.findByNameAndLevel(name, level);
    }

    public List<PolicyCategoryEntity> findAllActive() {
        return policyCategoryRepository.findAllByIsActiveTrue();
    }

    public List<PolicyCategoryMapping> findMappingsByPolicy(PolicyEntity policy) {
        return policyCategoryMappingRepository.findByPolicy(policy);
    }

    public Map<Long, List<PolicyCategoryMapping>> findMappingMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyCategoryMappingRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.groupingBy(mapping -> mapping.getPolicy().getId()));
    }

    @Transactional
    public PolicyCategoryEntity save(PolicyCategoryEntity category) {
        return policyCategoryRepository.save(category);
    }

    @Transactional
    public void deleteMappings(List<PolicyCategoryMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyCategoryMappingRepository.deleteAll(mappings);
        }
    }

    @Transactional
    public void saveMappings(List<PolicyCategoryMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyCategoryMappingRepository.saveAll(mappings);
        }
    }
}
