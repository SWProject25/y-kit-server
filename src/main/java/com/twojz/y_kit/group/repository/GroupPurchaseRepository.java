package com.twojz.y_kit.group.repository;

import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPurchaseRepository extends JpaRepository<GroupPurchaseEntity, Long> {

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    WHERE g.id = :groupPurchaseId
    """)
    Optional<GroupPurchaseWithCountsDto> findGroupPurchaseWithCountsById(
            @Param("groupPurchaseId") Long groupPurchaseId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    ORDER BY g.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCounts(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    WHERE g.title LIKE %:keyword%
    ORDER BY g.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCountsByKeyword(
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    WHERE g.status = :status
    ORDER BY g.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCountsByStatus(
            @Param("status") GroupPurchaseStatus status,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    WHERE g.region.code = :regionCode
    ORDER BY g.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCountsByRegionCode(
            @Param("regionCode") String regionCode,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        g.id,
        g.title,
        g.productName,
        g.productLink,
        g.price,
        g.minParticipants,
        g.maxParticipants,
        g.currentParticipants,
        g.deadline,
        g.status,
        g.region.code,
        g.region.name,
        (SELECT COUNT(l2) FROM GroupPurchaseLikeEntity l2 WHERE l2.groupPurchase = g),
        (SELECT COUNT(c2) FROM GroupPurchaseCommentEntity c2 WHERE c2.groupPurchase = g),
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseLikeEntity l WHERE l.groupPurchase = g AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM GroupPurchaseBookmarkEntity b WHERE b.groupPurchase = g AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM GroupPurchaseEntity g
    WHERE g.user.id = :userId
    ORDER BY g.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCountsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto(
        gp.id,
        gp.title,
        gp.productName,
        gp.productLink,
        gp.price,
        gp.minParticipants,
        gp.maxParticipants,
        gp.currentParticipants,
        gp.deadline,
        gp.status,
        gp.region.code,
        gp.region.name,
        COUNT(DISTINCT gpl.id),
        COUNT(DISTINCT gpc.id),
        CASE WHEN SUM(CASE WHEN gpl.user.id = :userId THEN 1 ELSE 0 END) > 0 THEN true ELSE false END,
        CASE WHEN SUM(CASE WHEN gpb.user.id = :userId THEN 1 ELSE 0 END) > 0 THEN true ELSE false END
    )
    FROM GroupPurchaseEntity gp
    LEFT JOIN GroupPurchaseLikeEntity gpl ON gpl.groupPurchase.id = gp.id
    LEFT JOIN GroupPurchaseCommentEntity gpc ON gpc.groupPurchase.id = gp.id
    LEFT JOIN GroupPurchaseBookmarkEntity gpb ON gpb.groupPurchase.id = gp.id
    WHERE gp.status = :status
      AND gp.region.code = :regionCode
    GROUP BY gp.id, gp.region.code, gp.region.name
    ORDER BY gp.createdAt DESC
    """)
    Page<GroupPurchaseWithCountsDto> findGroupPurchasesWithCountsByStatusAndRegionCode(
            @Param("status") GroupPurchaseStatus status,
            @Param("regionCode") String regionCode,
            @Param("userId") Long userId,
            Pageable pageable
    );
}