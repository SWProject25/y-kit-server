package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyDetailRepository extends JpaRepository<PolicyDetailEntity, Long> {
    Optional<PolicyDetailEntity> findByPolicy(PolicyEntity policy);
    @Query("SELECT pd FROM PolicyDetailEntity pd WHERE pd.policy.id IN :policyIds")
    List<PolicyDetailEntity> findByPolicyIdIn(@Param("policyIds") Collection<Long> policyIds);
    void deleteByPolicy(PolicyEntity policy);
}
