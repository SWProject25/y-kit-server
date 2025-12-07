package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRegionRepository extends JpaRepository<PolicyRegion, Long> {
    List<PolicyRegion> findByPolicy(PolicyEntity policy);

    void deleteByPolicy(PolicyEntity policy);

    @Query("SELECT pr FROM PolicyRegion pr " +
            "JOIN FETCH pr.region " +
            "WHERE pr.policy IN :policies")
    List<PolicyRegion> findByPolicyIn(@Param("policies") List<PolicyEntity> policies);
}
