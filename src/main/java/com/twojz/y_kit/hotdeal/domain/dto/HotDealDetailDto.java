package com.twojz.y_kit.hotdeal.domain.dto;

import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import java.time.LocalDateTime;

/**
 * 핫딜 상세 조회용 DTO
 */
public record HotDealDetailDto(
        Long hotDealId,
        String title,
        String content,
        String placeName,
        String address,
        String url,
        Double latitude,
        Double longitude,

        // 분류
        DealType dealType,
        HotDealCategory category,

        // 작성자
        Long authorId,
        String authorName,

        // 카운트
        long likeCount,
        long commentCount,
        int viewCount,

        // 사용자 상태
        boolean isLiked,
        boolean isBookmarked,

        // 지역
        String regionCode,
        String regionName,

        // 시간
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}