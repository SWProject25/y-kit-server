package com.twojz.y_kit.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupPurchaseCommentCreateRequest {
    @Schema(description = "댓글 내용", example = "이번 상품 정말 기대돼요!")
    private String content;
}
