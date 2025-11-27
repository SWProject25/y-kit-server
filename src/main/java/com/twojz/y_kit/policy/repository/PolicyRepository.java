package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
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

    List<PolicyEntity> findAllByPolicyNoIn(Collection<String> policyNos);

    /**
     * 정책 목록 조회 - 기본
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findAllInfo(Pageable pageable);

    /**
     * 카테고리별 정책 조회
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.categoryMappings cm
                WHERE p.isActive = true
                AND cm.category.id = :categoryId
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 키워드 검색
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.keywordMappings km
                WHERE p.isActive = true
                AND (
                    LOWER(d.plcyNm) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(d.plcyExplnCn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(km.keyword.keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 지역별 정책 조회
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.regions pr
                WHERE p.isActive = true
                AND pr.region.code = :regionCode
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findByRegionCode(@Param("regionCode") String regionCode, Pageable pageable);

    /**
     * 연령대 맞는 정책 조회
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                AND (
                    (q.sprtTrgtMinAge IS NULL OR q.sprtTrgtMinAge <= :age)
                    AND (q.sprtTrgtMaxAge IS NULL OR q.sprtTrgtMaxAge >= :age)
                )
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findByAge(@Param("age") Integer age, Pageable pageable);

    /**
     * 신청 가능한 정책 조회
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                AND a.aplyBgngYmd <= :today
                AND a.aplyEndYmd >= :today
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findApplicationAvailable(@Param("today") LocalDate today, Pageable pageable);

    /**
     * 복합 조건 검색 - 키워드 + 연령
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.keywordMappings km
                WHERE p.isActive = true
                AND (
                    LOWER(d.plcyNm) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(d.plcyExplnCn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(km.keyword.keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (
                    (q.sprtTrgtMinAge IS NULL OR q.sprtTrgtMinAge <= :age)
                    AND (q.sprtTrgtMaxAge IS NULL OR q.sprtTrgtMaxAge >= :age)
                )
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findByKeywordAndAge(
            @Param("keyword") String keyword,
            @Param("age") Integer age,
            Pageable pageable
    );

    /**
     * 추천 정책 조회 (나이 + 지역 + 신청가능 + 카테고리)
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.regions pr
                LEFT JOIN p.categoryMappings cm
                WHERE p.isActive = true
                AND pr.region.code = :regionCode
                AND (
                    (q.sprtTrgtMinAge IS NULL OR q.sprtTrgtMinAge <= :age)
                    AND (q.sprtTrgtMaxAge IS NULL OR q.sprtTrgtMaxAge >= :age)
                )
                AND a.aplyBgngYmd <= :today
                AND a.aplyEndYmd >= :today
                AND (:categoryId IS NULL OR cm.category.id = :categoryId)
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findRecommendedWithCategory(
            @Param("age") Integer age,
            @Param("regionCode") String regionCode,
            @Param("today") LocalDate today,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    /**
     * 인기 정책 (조회수 순)
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                ORDER BY p.viewCount DESC, p.createdAt DESC
            """)
    Page<PolicyEntity> findPopularByViewCount(Pageable pageable);

    /**
     * 인기 정책 (북마크 순)
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                ORDER BY p.bookmarkCount DESC, p.createdAt DESC
            """)
    Page<PolicyEntity> findPopularByBookmarkCount(Pageable pageable);

    /**
     * 마감 임박 정책
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                WHERE p.isActive = true
                AND a.aplyBgngYmd <= :today
                AND a.aplyEndYmd >= :today
                ORDER BY a.aplyEndYmd ASC
            """)
    Page<PolicyEntity> findDeadlineSoon(@Param("today") LocalDate today, Pageable pageable);

    /**
     * 정책 상세 조회 (ID)
     */
    @Query("""
                SELECT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail
                LEFT JOIN FETCH p.application
                LEFT JOIN FETCH p.qualification
                LEFT JOIN FETCH p.document
                WHERE p.id = :policyId
            """)
    Optional<PolicyEntity> findByIdWithDetails(@Param("policyId") Long policyId);

    /**
     * 정책 상세 조회 (정책번호)
     */
    @Query("""
                SELECT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail
                LEFT JOIN FETCH p.application
                LEFT JOIN FETCH p.qualification
                LEFT JOIN FETCH p.document
                WHERE p.policyNo = :policyNo
            """)
    Optional<PolicyEntity> findByPolicyNoWithDetails(@Param("policyNo") String policyNo);

    /**
     * 카테고리 일괄 조회 (N+1 방지)
     */
    @Query("""
                SELECT p FROM PolicyEntity p
                LEFT JOIN FETCH p.categoryMappings cm
                LEFT JOIN FETCH cm.category
                WHERE p IN :policies
            """)
    List<PolicyEntity> findWithCategories(@Param("policies") List<PolicyEntity> policies);

    /**
     * 키워드 일괄 조회 (N+1 방지)
     */
    @Query("""
                SELECT p FROM PolicyEntity p
                LEFT JOIN FETCH p.keywordMappings km
                LEFT JOIN FETCH km.keyword
                WHERE p IN :policies
            """)
    List<PolicyEntity> findWithKeywords(@Param("policies") List<PolicyEntity> policies);

    /**
     * 지역 일괄 조회 (N+1 방지)
     */
    @Query("""
                SELECT p FROM PolicyEntity p
                LEFT JOIN FETCH p.regions pr
                LEFT JOIN FETCH pr.region
                WHERE p IN :policies
            """)
    List<PolicyEntity> findWithRegions(@Param("policies") List<PolicyEntity> policies);


    List<PolicyEntity> findByIsActive(Boolean isActive);

    // PolicyRepository.java에 추가할 검색 쿼리 메서드 (매핑 테이블 활용)

    /**
     * 키워드로 정책명/설명 검색 (PolicyDetailEntity에서 검색)
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.detail d " +
            "WHERE p.isActive = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     d.plcyNm LIKE %:keyword% OR " +
            "     d.plcyExplnCn LIKE %:keyword%)")
    Page<PolicyEntity> findByKeywordContaining(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 카테고리 ID + 키워드 ID + 텍스트 검색으로 정책 필터링
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.detail d " +
            "LEFT JOIN p.categoryMappings cm " +
            "LEFT JOIN p.keywordMappings km " +
            "WHERE p.isActive = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     d.plcyNm LIKE %:keyword% OR " +
            "     d.plcyExplnCn LIKE %:keyword%) " +
            "AND (:#{#categoryIds == null || #categoryIds.isEmpty()} = true OR cm.category.id IN :categoryIds) " +
            "AND (:#{#keywordIds == null || #keywordIds.isEmpty()} = true OR km.keyword.id IN :keywordIds)")
    Page<PolicyEntity> findByFilters(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keywordIds") List<Long> keywordIds,
            Pageable pageable
    );

    /**
     * 카테고리 ID만으로 필터링
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.categoryMappings cm " +
            "WHERE p.isActive = true " +
            "AND cm.category.id IN :categoryIds")
    Page<PolicyEntity> findByCategoryIds(
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable
    );

    /**
     * 키워드 ID만으로 필터링
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.keywordMappings km " +
            "WHERE p.isActive = true " +
            "AND km.keyword.id IN :keywordIds")
    Page<PolicyEntity> findByKeywordIds(
            @Param("keywordIds") List<Long> keywordIds,
            Pageable pageable
    );

    /**
     * 카테고리 ID + 키워드 ID 조합 필터링
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.categoryMappings cm " +
            "LEFT JOIN p.keywordMappings km " +
            "WHERE p.isActive = true " +
            "AND cm.category.id IN :categoryIds " +
            "AND km.keyword.id IN :keywordIds")
    Page<PolicyEntity> findByCategoryAndKeywordIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keywordIds") List<Long> keywordIds,
            Pageable pageable
    );

    /**
     * 카테고리 필터 + 텍스트 검색
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.detail d " +
            "LEFT JOIN p.categoryMappings cm " +
            "WHERE p.isActive = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     d.plcyNm LIKE %:keyword% OR " +
            "     d.plcyExplnCn LIKE %:keyword%) " +
            "AND cm.category.id IN :categoryIds")
    Page<PolicyEntity> findByCategoriesAndKeyword(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable
    );

    /**
     * 키워드 필터 + 텍스트 검색
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.detail d " +
            "LEFT JOIN p.keywordMappings km " +
            "WHERE p.isActive = true " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     d.plcyNm LIKE %:keyword% OR " +
            "     d.plcyExplnCn LIKE %:keyword%) " +
            "AND km.keyword.id IN :keywordIds")
    Page<PolicyEntity> findByKeywordsAndKeyword(
            @Param("keyword") String keyword,
            @Param("keywordIds") List<Long> keywordIds,
            Pageable pageable
    );
}
