package com.twojz.y_kit.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
