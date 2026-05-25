package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyQualificationRepository extends JpaRepository<PolicyQualificationEntity, Long> {
    Optional<PolicyQualificationEntity> findByPolicy(PolicyEntity policy);
    @Query("SELECT pq FROM PolicyQualificationEntity pq WHERE pq.policy.id IN :policyIds")
    List<PolicyQualificationEntity> findByPolicyIdIn(@Param("policyIds") Collection<Long> policyIds);
    void deleteByPolicy(PolicyEntity policy);
}
