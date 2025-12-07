package com.twojz.y_kit.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupPurchaseDetailResponse {
    @Schema(description = "공동구매 ID")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "공동 구매 내용")
    private String content;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "상품 링크")
    private String productLink;

    @Schema(description = "연락 수단")
    private String contact;

    @Schema(description = "가격")
    private BigDecimal price;

    @Schema(description = "최소 참여 인원")
    private Integer minParticipants;

    @Schema(description = "최대 참여 인원")
    private Integer maxParticipants;

    @Schema(description = "현재 참여 인원")
    private Integer currentParticipants;

    @Schema(description = "마감일")
    private LocalDate deadline;

    @Schema(description = "상태")
    private GroupPurchaseStatus status;

    @Schema(description = "지역")
    private String region;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "좋아요 여부", example = "true")
    @JsonProperty("isLiked")
    private boolean isLiked;

    @Schema(description = "북마크 여부", example = "false")
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    @Schema(description = "참여 여부 (사용자가 이미 참여 중인지)")
    @JsonProperty("isParticipating")
    private boolean isParticipating;

    @Schema(description = "좋아요 수")
    private long likeCount;

    @Schema(description = "댓글 수")
    private long commentCount;

    @Schema(description = "댓글 목록")
    private List<GroupPurchaseCommentResponse> comments;

    @Schema(description = "작성일", example = "2024-12-01T14:22:00")
    private LocalDateTime createdAt;

    public static GroupPurchaseDetailResponse from(GroupPurchaseEntity groupPurchase, boolean isLiked, boolean isBookmarked, boolean isParticipating,
            long likeCount, long commentCount, List<GroupPurchaseCommentResponse> comments) {
        return GroupPurchaseDetailResponse.builder()
                .id(groupPurchase.getId())
                .title(groupPurchase.getTitle())
                .content(groupPurchase.getContent())
                .productName(groupPurchase.getProductName())
                .productLink(groupPurchase.getProductLink())
                .price(groupPurchase.getPrice())
                .contact(groupPurchase.getContact())
                .minParticipants(groupPurchase.getMinParticipants())
                .maxParticipants(groupPurchase.getMaxParticipants())
                .currentParticipants(groupPurchase.getCurrentParticipants())
                .deadline(groupPurchase.getDeadline())
                .status(groupPurchase.getStatus())
                .region(groupPurchase.getRegion() != null ? groupPurchase.getRegion().getFullName() : null)
                .authorId(groupPurchase.getUser().getId())
                .authorName(groupPurchase.getUser().getNickName())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .isParticipating(isParticipating)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(groupPurchase.getCreatedAt())
                .comments(comments)
                .build();
    }
}