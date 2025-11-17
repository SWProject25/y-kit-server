package com.twojz.y_kit.group.dto.response;

import com.twojz.y_kit.group.domain.dto.GroupPurchaseWithCountsDto;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
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

    @Schema(description = "마감일")
    private LocalDateTime deadline;

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
                .status(dto.status())
                .deadline(dto.deadline())
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .build();
    }
}