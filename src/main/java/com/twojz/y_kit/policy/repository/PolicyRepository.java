package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {
    Optional<PolicyEntity> findByPolicyNo(String policyNo);

    /**
     * 정책 상세 조회 (모든 연관 데이터 fetch join)
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail
        LEFT JOIN FETCH p.application
        LEFT JOIN FETCH p.qualification
        WHERE p.id = :id AND p.isActive = true
    """)
    Optional<PolicyEntity> findByIdWithAll(@Param("id") Long id);

    /**
     * 정책 번호로 상세 조회
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail
        LEFT JOIN FETCH p.application
        LEFT JOIN FETCH p.qualification
        WHERE p.policyNo = :policyNo AND p.isActive = true
    """)
    Optional<PolicyEntity> findByPolicyNoWithAll(@Param("policyNo") String policyNo);

    /**
     * 정책 리스트 조회 (페이징)
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail d
        WHERE p.isActive = true
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findAllWithBasicInfo(Pageable pageable);

    /**
     * 카테고리별 정책 조회
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail d
        JOIN p.categoryMappings cm
        WHERE cm.category.id = :categoryId AND p.isActive = true
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 키워드로 정책 검색
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail d
        JOIN p.keywordMappings km
        WHERE km.keyword.keyword = :keyword AND p.isActive = true
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 정책명으로 검색
     */
    @Query("""
        SELECT DISTINCT p FROM PolicyEntity p
        LEFT JOIN FETCH p.detail d
        WHERE d.plcyNm LIKE %:keyword% AND p.isActive = true
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> searchByPolicyName(@Param("keyword") String keyword, Pageable pageable);

}
