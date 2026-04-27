package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.repository.PolicyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyDocumentCommandService {
    private final PolicyDocumentRepository policyDocumentRepository;

    public PolicyDocumentEntity save(PolicyDocumentEntity document) {
        return policyDocumentRepository.save(document);
    }
}
