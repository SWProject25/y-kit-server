package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "사용자 디바이스 정보 응답")
public class UserDeviceResponse {
    @Schema(description = "디바이스 ID")
    private Long id;

    @Schema(description = "디바이스명", example = "Chrome on Windows")
    private String deviceName;

    @Schema(description = "디바이스 토큰 (전체)", example = "dcR79hw8TgUVqkH6lRcurB:APA91b...")
    private String deviceToken;

    @Schema(description = "활성 상태", example = "true")
    private boolean isActive;

    @Schema(description = "알림 허용 여부", example = "true")
    private boolean notificationEnabled;

    @Schema(description = "마지막 로그인", example = "2025-12-05T14:22:00")
    private LocalDateTime lastLogin;

    @Schema(description = "등록일", example = "2025-12-01T10:00:00")
    private LocalDateTime createdAt;

    public static UserDeviceResponse from(UserDeviceEntity device) {
        return UserDeviceResponse.builder()
                .id(device.getId())
                .deviceName(device.getDeviceName())
                .deviceToken(device.getDeviceToken())
                .isActive(device.isActive())
                .notificationEnabled(device.isNotificationEnabled())
                .lastLogin(device.getLastLogin())
                .createdAt(device.getCreatedAt())
                .build();
    }
}