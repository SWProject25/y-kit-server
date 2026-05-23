package com.twojz.y_kit.policy.service.persistence;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyDocumentRepository;
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

    @Transactional
    public PolicyDocumentEntity save(PolicyDocumentEntity document) {
        return policyDocumentRepository.save(document);
    }
}
