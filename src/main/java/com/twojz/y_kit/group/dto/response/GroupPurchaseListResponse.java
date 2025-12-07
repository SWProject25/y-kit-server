package com.twojz.y_kit.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseCategory;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupPurchaseListResponse {
    @Schema(description = "공동구매 ID")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "카테고리")
    private GroupPurchaseCategory category;

    @Schema(description = "가격")
    private BigDecimal price;

    @Schema(description = "상태")
    private GroupPurchaseStatus status;

    @Schema(description = "현재 참여 인원")
    private Integer currentParticipants;

    @Schema(description = "최대 참여 인원")
    private Integer maxParticipants;

    @Schema(description = "마감일")
    private LocalDate deadline;

    @Schema(description = "지역")
    private String region;

    @Schema(description = "좋아요 수")
    private long likeCount;

    @Schema(description = "댓글 수")
    private long commentCount;

    @Schema(description = "조회 수")
    private int viewCount;

    @Schema(description = "좋아요 여부", example = "true")
    @JsonProperty("isLiked")
    private boolean isLiked;

    @Schema(description = "북마크 여부", example = "false")
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    @Schema(description = "참여 여부")
    @JsonProperty("isParticipating")
    private boolean isParticipating;

    @Schema(description = "작성일", example = "2024-12-01T14:22:00")
    private LocalDateTime createdAt;

    public static GroupPurchaseListResponse from(GroupPurchaseEntity groupPurchase, boolean isLiked, boolean isBookmarked, boolean isParticipating, long likeCount, long commentCount) {
        return GroupPurchaseListResponse.builder()
                .id(groupPurchase.getId())
                .title(groupPurchase.getTitle())
                .productName(groupPurchase.getProductName())
                .price(groupPurchase.getPrice())
                .maxParticipants(groupPurchase.getMaxParticipants())
                .currentParticipants(groupPurchase.getCurrentParticipants())
                .deadline(groupPurchase.getDeadline())
                .status(groupPurchase.getStatus())
                .region(groupPurchase.getRegion() != null ? groupPurchase.getRegion().getFullName() : null)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .viewCount(groupPurchase.getViewCount())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .isParticipating(isParticipating)
                .createdAt(groupPurchase.getCreatedAt())
                .build();
    }
}