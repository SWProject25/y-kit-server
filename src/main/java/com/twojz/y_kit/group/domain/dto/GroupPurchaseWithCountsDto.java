package com.twojz.y_kit.group.domain.dto;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GroupPurchaseWithCountsDto(
        Long groupPurchaseId,
        String title,
        String productName,
        String productLink,
        BigDecimal price,
        Integer minParticipants,
        Integer maxParticipants,
        Integer currentParticipants,
        LocalDateTime deadline,
        GroupPurchaseStatus status,
        String regionCode,
        String regionName,
        long likeCount,
        long commentCount,
        boolean isLiked,
        boolean isBookmarked
) {}
