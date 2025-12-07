package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyCategoryMappingRepository extends JpaRepository<PolicyCategoryMapping, Long> {
    List<PolicyCategoryMapping> findByPolicy(PolicyEntity policy);

    @Query("SELECT m FROM PolicyCategoryMapping m " +
            "JOIN FETCH m.category " +
            "WHERE m.policy IN :policies")
    List<PolicyCategoryMapping> findByPolicyIn(@Param("policies") List<PolicyEntity> policies);

    void deleteByPolicy(PolicyEntity policy);
}
