package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.user.entity.UserEntity;
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

    @EntityGraph(attributePaths = {"user", "region"})
    @Query("SELECT DISTINCT g FROM GroupPurchaseEntity g " +
            "WHERE (:status IS NULL OR g.status = :status) " +
            "AND (:regionCode IS NULL OR g.region.code = :regionCode) " +
            "AND (" +
            "(:keyword1 IS NULL) OR " +
            "(g.title LIKE %:keyword1% OR g.productName LIKE %:keyword1% OR g.content LIKE %:keyword1%) OR " +
            "(:keyword2 IS NOT NULL AND (g.title LIKE %:keyword2% OR g.productName LIKE %:keyword2% OR g.content LIKE %:keyword2%)) OR " +
            "(:keyword3 IS NOT NULL AND (g.title LIKE %:keyword3% OR g.productName LIKE %:keyword3% OR g.content LIKE %:keyword3%)) OR " +
            "(:keyword4 IS NOT NULL AND (g.title LIKE %:keyword4% OR g.productName LIKE %:keyword4% OR g.content LIKE %:keyword4%)) OR " +
            "(:keyword5 IS NOT NULL AND (g.title LIKE %:keyword5% OR g.productName LIKE %:keyword5% OR g.content LIKE %:keyword5%))" +
            ")")
    Page<GroupPurchaseEntity> searchByKeywords(
            @Param("status") GroupPurchaseStatus status,
            @Param("regionCode") String regionCode,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            @Param("keyword4") String keyword4,
            @Param("keyword5") String keyword5,
            Pageable pageable
    );
}