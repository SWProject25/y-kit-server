package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.repository.PolicyQualificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyQualificationCommandService {
    private final PolicyQualificationRepository policyQualificationRepository;

    public PolicyQualificationEntity save(PolicyQualificationEntity qualification) {
        return policyQualificationRepository.save(qualification);
    }
}
