package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRegionRepository extends JpaRepository<PolicyRegion, Long> {
    List<PolicyRegion> findByPolicy(PolicyEntity policy);
    void deleteByPolicy(PolicyEntity policy);
}
