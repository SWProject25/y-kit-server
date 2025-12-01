package com.twojz.y_kit.group.dto.response;

import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
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

    @Schema(description = "가격")
    private BigDecimal price;

    @Schema(description = "상태")
    private GroupPurchaseStatus status;

    @Schema(description = "현재 참여 인원")
    private Integer currentParticipants;

    @Schema(description = "마감일")
    private LocalDate deadline;

    @Schema(description = "지역")
    private String region;

    @Schema(description = "좋아요 수")
    private long likeCount;

    @Schema(description = "댓글 수")
    private long commentCount;

    public static GroupPurchaseListResponse fromDto(GroupPurchaseWithCountsDto dto) {
        return GroupPurchaseListResponse.builder()
                .id(dto.groupPurchaseId())
                .title(dto.title())
                .productName(dto.productName())
                .price(dto.price())
                .currentParticipants(dto.currentParticipants())
                .status(dto.status())
                .deadline(dto.deadline())
                .region(dto.region())
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .build();
    }
}