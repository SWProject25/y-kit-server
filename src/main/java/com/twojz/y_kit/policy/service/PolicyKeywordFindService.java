package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import com.twojz.y_kit.policy.repository.PolicyKeywordRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyKeywordFindService {
    private final PolicyKeywordRepository policyKeywordRepository;

    public Optional<PolicyKeywordEntity> findByKeyword(String keyword) {
        return policyKeywordRepository.findByKeyword(keyword);
    }

    public List<PolicyKeywordEntity> findTop50ByUsageCount() {
        return policyKeywordRepository.findTop50ByOrderByUsageCountDesc();
    }
}
