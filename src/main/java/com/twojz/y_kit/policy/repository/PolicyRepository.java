package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
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
                WHERE p.isActive = true
                AND (pr.region.code = :regionCode OR pr IS NULL)
                AND (
                    q IS NULL 
                    OR (
                        (q.sprtTrgtMinAge IS NULL OR q.sprtTrgtMinAge <= :age)
                        AND (q.sprtTrgtMaxAge IS NULL OR q.sprtTrgtMaxAge >= :age)
                    )
                )
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findRecommendedWithCategory(
            @Param("age") Integer age,
            @Param("regionCode") String regionCode,
            Pageable pageable
    );

    /**
     * 사용자 프로필 기반 정밀 추천 (나이 + 지역 + 성별 + 취업상태 + 학력 + 전공)
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.regions pr
                WHERE p.isActive = true
                AND (pr.region.code = :regionCode OR pr IS NULL)
                AND (
                    q IS NULL
                    OR (
                        (q.sprtTrgtMinAge IS NULL OR q.sprtTrgtMinAge <= :age)
                        AND (q.sprtTrgtMaxAge IS NULL OR q.sprtTrgtMaxAge >= :age)
                        AND (q.jobCd IS NULL OR q.jobCd = :employmentStatus)
                        AND (q.schoolCd IS NULL OR q.schoolCd = :educationLevel)
                        AND (q.plcyMajorCd IS NULL OR q.plcyMajorCd = :major)
                    )
                )
                ORDER BY p.createdAt DESC
            """)
    Page<PolicyEntity> findRecommendedWithProfile(
            @Param("age") Integer age,
            @Param("regionCode") String regionCode,
            @Param("employmentStatus") EmploymentStatus employmentStatus,
            @Param("educationLevel") EducationLevel educationLevel,
            @Param("major") MajorField major,
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

    /**
     * 통합 검색 - 모든 필터 조건을 null 가능하게 처리 - categoryIds, keywordIds, keyword1~5 모두 선택적 - 형태소 분석 지원 (keyword1~5)
     */
    @Query("SELECT DISTINCT p FROM PolicyEntity p " +
            "LEFT JOIN p.detail d " +
            "LEFT JOIN p.categoryMappings cm " +
            "LEFT JOIN p.keywordMappings km " +
            "WHERE p.isActive = true " +
            "AND (:#{#categoryIds == null || #categoryIds.isEmpty()} = true OR cm.category.id IN :categoryIds) " +
            "AND (:#{#keywordIds == null || #keywordIds.isEmpty()} = true OR km.keyword.id IN :keywordIds) " +
            "AND (" +
            "    (:keyword1 IS NULL AND :keyword2 IS NULL AND :keyword3 IS NULL AND :keyword4 IS NULL AND :keyword5 IS NULL) "
            +
            "    OR (:keyword1 IS NOT NULL AND (d.plcyNm LIKE %:keyword1% OR d.plcyExplnCn LIKE %:keyword1%)) " +
            "    OR (:keyword2 IS NOT NULL AND (d.plcyNm LIKE %:keyword2% OR d.plcyExplnCn LIKE %:keyword2%)) " +
            "    OR (:keyword3 IS NOT NULL AND (d.plcyNm LIKE %:keyword3% OR d.plcyExplnCn LIKE %:keyword3%)) " +
            "    OR (:keyword4 IS NOT NULL AND (d.plcyNm LIKE %:keyword4% OR d.plcyExplnCn LIKE %:keyword4%)) " +
            "    OR (:keyword5 IS NOT NULL AND (d.plcyNm LIKE %:keyword5% OR d.plcyExplnCn LIKE %:keyword5%))" +
            ")")
    Page<PolicyEntity> searchPoliciesUnified(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keywordIds") List<Long> keywordIds,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            @Param("keyword4") String keyword4,
            @Param("keyword5") String keyword5,
            Pageable pageable
    );

    @Query("SELECT p FROM PolicyEntity p " +
            "WHERE p.aiAnalysis IS NULL AND p.isActive = true " +
            "ORDER BY p.id ASC")
    Page<PolicyEntity> findAllByAiAnalysisIsNull(Pageable pageable);

    @Query("SELECT COUNT(p) FROM PolicyEntity p " +
            "WHERE p.aiAnalysis IS NULL AND p.isActive = true")
    long countByAiAnalysisIsNull();

    /**
     * 유사 정책 조회 (같은 카테고리 기반)
     */
    @Query("""
                SELECT DISTINCT p FROM PolicyEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.application a
                LEFT JOIN FETCH p.qualification q
                LEFT JOIN p.categoryMappings cm
                WHERE p.isActive = true
                AND p.id != :policyId
                AND cm.category.id IN (
                    SELECT cm2.category.id FROM PolicyEntity p2
                    LEFT JOIN p2.categoryMappings cm2
                    WHERE p2.id = :policyId
                )
                ORDER BY p.viewCount DESC, p.createdAt DESC
            """)
    List<PolicyEntity> findSimilarPoliciesByCategory(@Param("policyId") Long policyId, Pageable pageable);
}