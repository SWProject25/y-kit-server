package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocumentEntity, Long> {
    List<PolicyDocumentEntity> findByPolicy(PolicyEntity policy);
    void deleteByPolicy(PolicyEntity policy);
}
