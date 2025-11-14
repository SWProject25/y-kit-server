package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocumentEntity, Long> {
    Optional<PolicyDocumentEntity> findByPolicy(PolicyEntity policy);
    void deleteByPolicy(PolicyEntity policy);
}
