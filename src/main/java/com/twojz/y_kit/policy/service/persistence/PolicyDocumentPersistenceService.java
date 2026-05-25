package com.twojz.y_kit.policy.service.persistence;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyDocumentRepository;
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
public class PolicyDocumentPersistenceService {

    private final PolicyDocumentRepository policyDocumentRepository;

    public PolicyDocumentEntity findNullableByPolicy(PolicyEntity policy) {
        return policyDocumentRepository.findByPolicy(policy).orElse(null);
    }

    public Map<Long, PolicyDocumentEntity> findMapByPolicies(Collection<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) {
            return Map.of();
        }
        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();
        return policyDocumentRepository.findByPolicyIdIn(policyIds).stream()
                .collect(Collectors.toMap(document -> document.getPolicy().getId(), Function.identity()));
    }

    @Transactional
    public PolicyDocumentEntity save(PolicyDocumentEntity document) {
        return policyDocumentRepository.save(document);
    }
}
