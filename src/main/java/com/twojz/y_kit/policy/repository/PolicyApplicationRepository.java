package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyApplicationRepository extends JpaRepository<PolicyApplicationEntity, Long> {
    Optional<PolicyApplicationEntity> findByPolicy(PolicyEntity policy);
    @Query("SELECT pa FROM PolicyApplicationEntity pa WHERE pa.policy.id IN :policyIds")
    List<PolicyApplicationEntity> findByPolicyIdIn(@Param("policyIds") Collection<Long> policyIds);
    void deleteByPolicy(PolicyEntity policy);
}
