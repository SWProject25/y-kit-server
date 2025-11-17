package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealRepository extends JpaRepository<HotDealEntity, Long>, JpaSpecificationExecutor<HotDealEntity> {

    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.latitude,
        h.longitude,
        h.region.code,
        (SELECT COUNT(l2) FROM HotDealLikeEntity l2 WHERE l2.hotDeal = h),
        (SELECT COUNT(c2) FROM HotDealCommentEntity c2 WHERE c2.hotDeal = h),
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM HotDealEntity h
    WHERE h.id = :hotDealId
    """)
    Optional<HotDealWithCountsDto> findHotDealWithCountsById(
            @Param("hotDealId") Long hotDealId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.latitude,
        h.longitude,
        h.region.code,
        (SELECT COUNT(l2) FROM HotDealLikeEntity l2 WHERE l2.hotDeal = h),
        (SELECT COUNT(c2) FROM HotDealCommentEntity c2 WHERE c2.hotDeal = h),
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM HotDealEntity h
    """)
    Page<HotDealWithCountsDto> findHotDealsWithCounts(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.latitude,
        h.longitude,
        h.region.code,
        (SELECT COUNT(l2) FROM HotDealLikeEntity l2 WHERE l2.hotDeal = h),
        (SELECT COUNT(c2) FROM HotDealCommentEntity c2 WHERE c2.hotDeal = h),
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM HotDealEntity h
    """)
    List<HotDealWithCountsDto> findHotDealsWithCounts(@Param("userId") Long userId);

    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.latitude,
        h.longitude,
        h.region.code,
        (SELECT COUNT(l2) FROM HotDealLikeEntity l2 WHERE l2.hotDeal = h),
        (SELECT COUNT(c2) FROM HotDealCommentEntity c2 WHERE c2.hotDeal = h),
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true ELSE false END
    )
    FROM HotDealEntity h
    WHERE h.title LIKE %:keyword%
    """)
    Page<HotDealWithCountsDto> findHotDealsWithCountsByKeyword(
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            Pageable pageable
    );
}