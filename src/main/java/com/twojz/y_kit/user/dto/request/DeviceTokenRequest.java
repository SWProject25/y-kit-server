package com.twojz.y_kit.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {
    @Schema(description = "FCM 디바이스 토큰", example = "fQRKB7rQ3k...", required = true)
    @NotBlank(message = "디바이스 토큰은 필수입니다.")
    private String deviceToken;

    @Schema(description = "디바이스 이름 (선택)", example = "Galaxy S23")
    private String deviceName;
}
