package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.repository.PolicyDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyDetailCommandService {
    private final PolicyDetailRepository policyDetailRepository;

    public PolicyDetailEntity save(PolicyDetailEntity detail) {
        return policyDetailRepository.save(detail);
    }
}
