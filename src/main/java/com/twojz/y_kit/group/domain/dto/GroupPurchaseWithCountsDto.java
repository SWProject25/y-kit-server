package com.twojz.y_kit.group.domain.dto;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GroupPurchaseWithCountsDto(
        Long groupPurchaseId,
        String title,
        String content,
        String productName,
        String productLink,
        BigDecimal price,
        String contact,
        int minParticipants,
        int maxParticipants,
        int currentParticipants,
        LocalDate deadline,
        GroupPurchaseStatus status,
        String region,
        long likeCount,
        long commentCount,
        boolean isLiked,
        boolean isBookmarked
) {}
