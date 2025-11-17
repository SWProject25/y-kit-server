package com.twojz.y_kit.group.dto.request;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GroupPurchaseCreateRequest {
    @Schema(description = "그룹 구매 제목", example = "한정판 티셔츠 공동 구매")
    private String title;

    @Schema(description = "상품명", example = "Limited Edition T-shirt")
    private String productName;

    @Schema(description = "상품 링크", example = "https://example.com/product/123")
    private String productLink;

    @Schema(description = "상품 가격", example = "25000")
    private BigDecimal price;

    @Schema(description = "최소 참여 인원", example = "5")
    private Integer minParticipants;

    @Schema(description = "최대 참여 인원", example = "20")
    private Integer maxParticipants;

    @Schema(description = "모집 마감일", example = "2025-12-31T23:59:00")
    private LocalDateTime deadline;

    @Schema(description = "그룹 구매 상태", example = "OPEN")
    private GroupPurchaseStatus status;

    @Schema(description = "지역 코드", example = "1101053")
    private String regionCode;
}