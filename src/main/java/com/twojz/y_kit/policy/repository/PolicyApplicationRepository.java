package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyApplicationRepository extends JpaRepository<PolicyApplicationEntity, Long> {
    Optional<PolicyApplicationEntity> findByPolicy(PolicyEntity policy);
    List<PolicyApplicationEntity> findByPolicyIn(Collection<PolicyEntity> policies);
    void deleteByPolicy(PolicyEntity policy);
}
