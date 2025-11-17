package com.twojz.y_kit.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupPurchaseCommentCreateRequest {
    @Schema(description = "댓글 내용", example = "이번 상품 정말 기대돼요!")
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다")
    private String content;
}
