package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyCategoryMappingCommandService {
    private final com.twojz.y_kit.policy.repository.PolicyCategoryMappingRepository policyCategoryMappingRepository;

    public void deleteAll(List<PolicyCategoryMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyCategoryMappingRepository.deleteAll(mappings);
        }
    }

    public void saveAll(List<PolicyCategoryMapping> mappings) {
        if (!mappings.isEmpty()) {
            policyCategoryMappingRepository.saveAll(mappings);
        }
    }
}
