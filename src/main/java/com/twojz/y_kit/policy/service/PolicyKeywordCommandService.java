package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import com.twojz.y_kit.policy.repository.PolicyKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyKeywordCommandService {
    private final PolicyKeywordRepository policyKeywordRepository;

    public PolicyKeywordEntity save(PolicyKeywordEntity keyword) {
        return policyKeywordRepository.save(keyword);
    }
}
