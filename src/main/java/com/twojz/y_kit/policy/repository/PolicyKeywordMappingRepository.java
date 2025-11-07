package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyKeywordMappingRepository extends JpaRepository<PolicyKeywordMapping, Long> {
    @Query("SELECT pkm FROM PolicyKeywordMapping pkm JOIN FETCH pkm.keyword WHERE pkm.policy = :policy")
    List<PolicyKeywordMapping> findByPolicyWithKeyword(@Param("policy") PolicyEntity policy);

    List<PolicyKeywordMapping> findByPolicy(PolicyEntity policy);

    void deleteByPolicy(PolicyEntity policy);
}
