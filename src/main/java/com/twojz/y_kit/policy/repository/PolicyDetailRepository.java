package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDetailRepository extends JpaRepository<PolicyDetailEntity, Long> {
    Optional<PolicyDetailEntity> findByPolicy(PolicyEntity policy);
    List<PolicyDetailEntity> findByPolicyIn(Collection<PolicyEntity> policies);
    void deleteByPolicy(PolicyEntity policy);
}
