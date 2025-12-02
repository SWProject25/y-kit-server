package com.twojz.y_kit.group.dto.response;

import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Schema(description = "작성자 이름")
    private String authorName;

    @Schema(description = "마감일")
    private LocalDate deadline;

    @Schema(description = "상태")
    private GroupPurchaseStatus status;

    @Schema(description = "지역")
    private String region;

    @Schema(description = "좋아요 여부")
    private boolean isLiked;

    @Schema(description = "북마크 여부")
    private boolean isBookmarked;

    @Schema(description = "참여 여부 (사용자가 이미 참여 중인지)")
    private boolean isParticipating;

    @Schema(description = "좋아요 수")
    private long likeCount;

    @Schema(description = "댓글 수")
    private long commentCount;

    @Schema(description = "댓글 목록")
    private List<GroupPurchaseCommentResponse> comments;

    public static GroupPurchaseDetailResponse fromDto(
            GroupPurchaseWithCountsDto dto,
            List<GroupPurchaseCommentResponse> comments,
            String authorName,
            boolean isParticipating
    ) {
        return GroupPurchaseDetailResponse.builder()
                .id(dto.groupPurchaseId())
                .title(dto.title())
                .content(dto.content())
                .productName(dto.productName())
                .productLink(dto.productLink())
                .contact(dto.contact())
                .price(dto.price())
                .minParticipants(dto.minParticipants())
                .maxParticipants(dto.maxParticipants())
                .currentParticipants(dto.currentParticipants())
                .authorName(authorName)
                .deadline(dto.deadline())
                .status(dto.status())
                .region(dto.region())
                .isLiked(dto.isLiked())
                .isBookmarked(dto.isBookmarked())
                .isParticipating(isParticipating)
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .comments(comments)
                .build();
    }
}