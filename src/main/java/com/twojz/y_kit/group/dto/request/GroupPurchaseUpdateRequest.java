package com.twojz.y_kit.group.dto.request;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupPurchaseUpdateRequest {
    @Schema(description = "그룹 구매 제목", example = "한정판 티셔츠 공동 구매")
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @Schema(description = "상품명", example = "Limited Edition T-shirt")
    @NotBlank(message = "상품명은 필수입니다")
    private String productName;

    @Schema(description = "상품 링크", example = "https://example.com/product/123")
    private String productLink;

    @Schema(description = "상품 가격", example = "25000")
    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 양수여야 합니다")
    private BigDecimal price;

    @Schema(description = "최소 참여 인원", example = "5")
    @NotNull(message = "최소 참여 인원은 필수입니다")
    @Positive(message = "최소 참여 인원은 양수여야 합니다")
    private Integer minParticipants;

    @Schema(description = "최대 참여 인원", example = "20")
    @NotNull(message = "최대 참여 인원은 필수입니다")
    @Positive(message = "최대 참여 인원은 양수여야 합니다")
    private Integer maxParticipants;

    @Schema(description = "모집 마감일", example = "2025-12-31T23:59:00")
    @NotNull(message = "마감일은 필수입니다")
    @Future(message = "마감일은 미래 시간이어야 합니다")
    private LocalDateTime deadline;

    @Schema(description = "그룹 구매 상태", example = "OPEN")
    @NotNull(message = "상태는 필수입니다")
    private GroupPurchaseStatus status;

    @Schema(description = "지역 코드", example = "1101053")
    @NotBlank(message = "지역 코드는 필수입니다")
    private String regionCode;
}
