package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<PolicyEntity, Long>, PolicyQueryRepository {

    Optional<PolicyEntity> findByPolicyNo(String policyNo);

    List<PolicyEntity> findAllByIsActiveTrue();

    List<PolicyEntity> findAllByPolicyNoIn(Collection<String> policyNos);

    @Query("SELECT p FROM PolicyEntity p WHERE p.aiAnalysis IS NULL AND p.isActive = true ORDER BY p.id ASC")
    Page<PolicyEntity> findAllByAiAnalysisIsNull(Pageable pageable);

    @Query("SELECT COUNT(p) FROM PolicyEntity p WHERE p.aiAnalysis IS NULL")
    long countByAiAnalysisIsNull();

    @Query("SELECT COUNT(p) FROM PolicyEntity p WHERE p.aiAnalysis IS NOT NULL")
    long countByAiAnalysisIsNotNull();
}
