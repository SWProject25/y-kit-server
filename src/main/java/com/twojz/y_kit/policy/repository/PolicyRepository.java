package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.entity.PolicyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {
    Optional<PolicyEntity> findByPolicyNo(String policyNo);
}
