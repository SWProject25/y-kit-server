package com.twojz.y_kit.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeCreateRequest {

    @Schema(description = "뱃지 이름", example = "첫 글쓰기")
    @NotBlank(message = "뱃지 이름은 필수입니다.")
    private String name;

    @Schema(description = "뱃지 설명", example = "첫 번째 글을 작성한 사용자에게 부여되는 뱃지입니다.")
    private String description;

    @Schema(description = "뱃지 아이콘 URL", example = "https://example.com/badges/first-post.png")
    private String iconUrl;
}