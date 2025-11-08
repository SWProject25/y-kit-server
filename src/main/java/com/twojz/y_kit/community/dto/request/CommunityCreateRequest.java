package com.twojz.y_kit.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityCreateRequest {
    @Schema(description = "게시글 제목", example = "청년 정책 후기 공유합니다", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 작성해주세요.")
    private String title;

    @Schema(description = "게시글 내용", example = "이번에 청년 주거 지원 정책을 신청해봤는데...", required = true)
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @Schema(description = "카테고리", example = "POLICY_REVIEW", required = true)
    @NotNull(message = "카테고리는 필수입니다.")
    private String category;
}
