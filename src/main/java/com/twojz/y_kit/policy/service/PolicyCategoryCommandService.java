package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.repository.PolicyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyCategoryCommandService {
    private final PolicyCategoryRepository policyCategoryRepository;

    public PolicyCategoryEntity save(PolicyCategoryEntity category) {
        return policyCategoryRepository.save(category);
    }
}
