package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.repository.PolicyCategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyCategoryFindService {
    private final PolicyCategoryRepository policyCategoryRepository;

    public Optional<PolicyCategoryEntity> findByNameAndLevel(String name, Integer level) {
        return policyCategoryRepository.findByNameAndLevel(name, level);
    }

    public List<PolicyCategoryEntity> findAllActive() {
        return policyCategoryRepository.findAllByIsActiveTrue();
    }
}
