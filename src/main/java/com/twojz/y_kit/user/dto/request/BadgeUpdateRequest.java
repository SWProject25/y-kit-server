package com.twojz.y_kit.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeUpdateRequest {

    @Schema(description = "뱃지 이름", example = "첫 글쓰기 마스터")
    private String name;

    @Schema(description = "뱃지 설명", example = "첫 번째 글을 작성하고 10개 이상의 좋아요를 받은 사용자")
    private String description;

    @Schema(description = "뱃지 아이콘 URL", example = "https://example.com/badges/first-post-master.png")
    private String iconUrl;
}
