package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyKeywordMappingRepository extends JpaRepository<PolicyKeywordMapping, Long> {
    @EntityGraph(attributePaths = "keyword")
    @Query("SELECT pkm FROM PolicyKeywordMapping pkm WHERE pkm.policy = :policy")
    List<PolicyKeywordMapping> findByPolicyWithKeyword(@Param("policy") PolicyEntity policy);

    List<PolicyKeywordMapping> findByPolicy(PolicyEntity policy);

    void deleteByPolicy(PolicyEntity policy);

    @Query("SELECT m FROM PolicyKeywordMapping m " +
            "JOIN FETCH m.keyword " +
            "WHERE m.policy IN :policies")
    List<PolicyKeywordMapping> findByPolicyIn(@Param("policies") List<PolicyEntity> policies);
}

