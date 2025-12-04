package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseRepository extends JpaRepository<GroupPurchaseEntity, Long> {
    @EntityGraph(attributePaths = {"user", "region"})
    Page<GroupPurchaseEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "region"})
    @Query("SELECT g FROM GroupPurchaseEntity g " +
            "WHERE (:status IS NULL OR g.status = :status) " +
            "AND (:regionCode IS NULL OR g.region.code = :regionCode)")
    Page<GroupPurchaseEntity> findByFilters(
            @Param("status") GroupPurchaseStatus status,
            @Param("regionCode") String regionCode,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "region"})
    Page<GroupPurchaseEntity> findByUser(UserEntity user, Pageable pageable);

    // 단일 키워드 검색 (제목 또는 상품명)
    @EntityGraph(attributePaths = {"user", "region"})
    Page<GroupPurchaseEntity> findByTitleContainingOrProductNameContaining(
            String title,
            String productName,
            Pageable pageable
    );

    // 통합 검색: 여러 키워드 + 상태/지역 필터 (모두 null 허용, AND 조건)
    @EntityGraph(attributePaths = {"user", "region"})
    @Query("SELECT DISTINCT g FROM GroupPurchaseEntity g " +
            "WHERE (:status IS NULL OR g.status = :status) " +
            "AND (:regionCode IS NULL OR g.region.code = :regionCode) " +
            "AND (" +
            "(:keyword1 IS NULL OR g.title LIKE %:keyword1% OR g.productName LIKE %:keyword1% OR g.content LIKE %:keyword1%) AND " +
            "(:keyword2 IS NULL OR g.title LIKE %:keyword2% OR g.productName LIKE %:keyword2% OR g.content LIKE %:keyword2%) AND " +
            "(:keyword3 IS NULL OR g.title LIKE %:keyword3% OR g.productName LIKE %:keyword3% OR g.content LIKE %:keyword3%) AND " +
            "(:keyword4 IS NULL OR g.title LIKE %:keyword4% OR g.productName LIKE %:keyword4% OR g.content LIKE %:keyword4%) AND " +
            "(:keyword5 IS NULL OR g.title LIKE %:keyword5% OR g.productName LIKE %:keyword5% OR g.content LIKE %:keyword5%))")
    Page<GroupPurchaseEntity> searchByKeywordsWithFilters(
            @Param("status") GroupPurchaseStatus status,
            @Param("regionCode") String regionCode,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            @Param("keyword4") String keyword4,
            @Param("keyword5") String keyword5,
            Pageable pageable
    );

    // 단일 키워드 + 상태/지역 필터 (모두 null 허용)
    @EntityGraph(attributePaths = {"user", "region"})
    @Query("SELECT g FROM GroupPurchaseEntity g " +
            "WHERE (:status IS NULL OR g.status = :status) " +
            "AND (:regionCode IS NULL OR g.region.code = :regionCode) " +
            "AND (g.title LIKE %:keyword% OR g.productName LIKE %:keyword% OR g.content LIKE %:keyword%)")
    Page<GroupPurchaseEntity> searchByKeywordWithFilters(
            @Param("status") GroupPurchaseStatus status,
            @Param("regionCode") String regionCode,
            @Param("keyword") String keyword,
            Pageable pageable
    );


}