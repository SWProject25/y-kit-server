package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyCategoryRepository extends JpaRepository<PolicyCategoryEntity, Long> {
    Optional<PolicyCategoryEntity> findByNameAndLevel(String name, Integer level);
    Optional<PolicyCategoryEntity> findByNameAndLevelAndParent(
            String name,
            Integer level,
            PolicyCategoryEntity parent
    );

    List<PolicyCategoryEntity> findByLevel(Integer level);

    List<PolicyCategoryEntity> findByParent(PolicyCategoryEntity parent);
}
