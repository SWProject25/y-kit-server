package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.repository.PolicyApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyApplicationCommandService {
    private final PolicyApplicationRepository policyApplicationRepository;

    public PolicyApplicationEntity save(PolicyApplicationEntity application) {
        return policyApplicationRepository.save(application);
    }
}
