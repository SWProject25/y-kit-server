package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyKeywordEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyKeywordRepository extends JpaRepository<PolicyKeywordEntity, Long> {
    Optional<PolicyKeywordEntity> findByKeyword(String keyword);
}
