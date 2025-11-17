package com.twojz.y_kit.hotdeal.domain.dto;

public record HotDealWithCountsDto(
        Long hotDealId,
        String title,
        String placeName,
        String address,
        String url,
        Double latitude,
        Double longitude,
        String regionCode,
        long likeCount,        // Long -> long
        long commentCount,     // Long -> long
        boolean isLiked,       // Boolean -> boolean
        boolean isBookmarked   // Boolean -> boolean
) {}
