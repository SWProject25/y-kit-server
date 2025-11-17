package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.user.entity.UserBadgeEntity;
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
public class UserBadgeResponse {

    @Schema(description = "사용자 뱃지 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "10")
    private Long userId;

    @Schema(description = "뱃지 정보")
    private BadgeResponse badge;

    @Schema(description = "획득 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime acquiredAt;

    public static UserBadgeResponse from(UserBadgeEntity userBadge) {
        return UserBadgeResponse.builder()
                .id(userBadge.getId())
                .userId(userBadge.getUser().getId())
                .badge(BadgeResponse.from(userBadge.getBadge()))
                .acquiredAt(userBadge.getAcquiredAt())
                .build();
    }
}
