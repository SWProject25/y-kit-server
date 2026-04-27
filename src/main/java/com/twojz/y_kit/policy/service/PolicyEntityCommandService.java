package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyEntityCommandService {
    private final PolicyRepository policyRepository;

    public PolicyEntity save(PolicyEntity policy) {
        return policyRepository.save(policy);
    }

    public List<PolicyEntity> saveAll(List<PolicyEntity> policies) {
        return policyRepository.saveAll(policies);
    }
}
