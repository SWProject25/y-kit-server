package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.user.entity.BadgeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {

    @Schema(description = "뱃지 ID", example = "1")
    private Long id;

    @Schema(description = "뱃지 이름", example = "첫 글쓰기")
    private String name;

    @Schema(description = "뱃지 설명", example = "첫 번째 글을 작성한 사용자에게 부여되는 뱃지입니다.")
    private String description;

    @Schema(description = "뱃지 아이콘 URL", example = "https://example.com/badges/first-post.png")
    private String iconUrl;

    @Schema(description = "생성 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime updatedAt;

    public static BadgeResponse from(BadgeEntity badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .createdAt(badge.getCreatedAt())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }
}
