package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocumentEntity, Long> {
    Optional<PolicyDocumentEntity> findByPolicy(PolicyEntity policy);

    @Query("SELECT pd FROM PolicyDocumentEntity pd WHERE pd.policy.id IN :policyIds")
    List<PolicyDocumentEntity> findByPolicyIdIn(@Param("policyIds") List<Long> policyIds);

    void deleteByPolicy(PolicyEntity policy);
}
