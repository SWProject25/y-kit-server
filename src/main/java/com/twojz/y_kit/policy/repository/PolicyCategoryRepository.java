package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.entity.PolicyCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyCategoryRepository extends JpaRepository<PolicyCategoryEntity, Long> {
    Optional<PolicyCategoryEntity> findByNameAndLevel(String name, Integer level);
}
