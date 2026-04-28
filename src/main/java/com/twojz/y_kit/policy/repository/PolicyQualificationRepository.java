package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyQualificationRepository extends JpaRepository<PolicyQualificationEntity, Long> {
    Optional<PolicyQualificationEntity> findByPolicy(PolicyEntity policy);
    List<PolicyQualificationEntity> findByPolicyIn(Collection<PolicyEntity> policies);
    void deleteByPolicy(PolicyEntity policy);
}
