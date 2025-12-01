package com.twojz.y_kit.hotdeal.repository;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealDetailDto;
import com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotDealRepository extends JpaRepository<HotDealEntity, Long> {
    /**
     * 핫딜 목록 조회
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.dealType,
        h.category,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true 
             ELSE false END,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true 
             ELSE false END,
        h.region.code,
        h.region.name,
        h.createdAt,
        h.expiresAt
    )
    FROM HotDealEntity h
    ORDER BY h.createdAt DESC
    """)
    Page<HotDealListDto> findHotDealList(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 핫딜 검색 (다중 필터)
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.dealType,
        h.category,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true 
             ELSE false END,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true 
             ELSE false END,
        h.region.code,
        h.region.name,
        h.createdAt,
        h.expiresAt
    )
    FROM HotDealEntity h
    WHERE (:keyword IS NULL OR h.title LIKE %:keyword% OR h.placeName LIKE %:keyword%)
      AND (:dealType IS NULL OR h.dealType = :dealType)
      AND (:category IS NULL OR h.category = :category)
      AND (:regionCode IS NULL OR h.region.code = :regionCode)
    ORDER BY h.createdAt DESC
    """)
    Page<HotDealListDto> searchHotDeals(
            @Param("keyword") String keyword,
            @Param("dealType") DealType dealType,
            @Param("category") HotDealCategory category,
            @Param("regionCode") String regionCode,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 좋아요한 핫딜 목록
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.dealType,
        h.category,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        true,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true 
             ELSE false END,
        h.region.code,
        h.region.name,
        h.createdAt,
        h.expiresAt
    )
    FROM HotDealLikeEntity l
    JOIN l.hotDeal h
    WHERE l.user.id = :userId
    ORDER BY l.createdAt DESC
    """)
    Page<HotDealListDto> findLikedHotDeals(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 북마크한 핫딜 목록
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.dealType,
        h.category,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true 
             ELSE false END,
        true,
        h.region.code,
        h.region.name,
        h.createdAt,
        h.expiresAt
    )
    FROM HotDealBookmarkEntity b
    JOIN b.hotDeal h
    WHERE b.user.id = :userId
    ORDER BY b.createdAt DESC
    """)
    Page<HotDealListDto> findBookmarkedHotDeals(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 내가 작성한 핫딜 목록
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto(
        h.id,
        h.title,
        h.placeName,
        h.address,
        h.url,
        h.dealType,
        h.category,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true 
             ELSE false END,
        CASE WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true 
             ELSE false END,
        h.region.code,
        h.region.name,
        h.createdAt,
        h.expiresAt
    )
    FROM HotDealEntity h
    WHERE h.user.id = :userId
    ORDER BY h.createdAt DESC
    """)
    Page<HotDealListDto> findMyHotDeals(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 핫딜 상세 조회
     */
    @Query("""
    SELECT new com.twojz.y_kit.hotdeal.domain.dto.HotDealDetailDto(
        h.id,
        h.title,
        h.content,
        h.placeName,
        h.address,
        h.url,
        h.latitude,
        h.longitude,
        h.dealType,
        h.category,
        h.user.id,
        h.user.name,
        h.likeCount,
        h.commentCount,
        h.viewCount,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealLikeEntity l WHERE l.hotDeal = h AND l.user.id = :userId) THEN true 
             ELSE false END,
        CASE WHEN :userId IS NULL THEN false 
             WHEN EXISTS(SELECT 1 FROM HotDealBookmarkEntity b WHERE b.hotDeal = h AND b.user.id = :userId) THEN true 
             ELSE false END,
        h.region.code,
        h.region.name,
        h.expiresAt,
        h.createdAt,
        h.updatedAt
    )
    FROM HotDealEntity h
    WHERE h.id = :hotDealId
    """)
    Optional<HotDealDetailDto> findHotDealDetail(
            @Param("hotDealId") Long hotDealId,
            @Param("userId") Long userId
    );
}