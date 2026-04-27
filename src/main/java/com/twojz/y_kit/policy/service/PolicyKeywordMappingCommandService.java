package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyKeywordMappingCommandService {
    private final com.twojz.y_kit.policy.repository.PolicyKeywordMappingRepository policyKeywordMappingRepository;

    public void deleteAll(List<PolicyKeywordMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyKeywordMappingRepository.deleteAll(mappings);
        }
    }

    public void saveAll(List<PolicyKeywordMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyKeywordMappingRepository.saveAll(mappings);
        }
    }
}
