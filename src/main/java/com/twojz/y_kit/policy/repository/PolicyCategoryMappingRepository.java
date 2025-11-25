package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyCategoryMappingRepository extends JpaRepository<PolicyCategoryMapping, Long> {
    List<PolicyCategoryMapping> findByPolicy(PolicyEntity policy);
    void deleteByPolicy(PolicyEntity policy);
}
