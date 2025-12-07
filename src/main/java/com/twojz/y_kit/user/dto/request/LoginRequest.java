package com.twojz.y_kit.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Schema(description = "이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Schema(description = "FCM 디바이스 토큰 (선택)")
    private String deviceToken;

    @Schema(description = "디바이스 이름 (선택)", example = "iPhone 15 Pro")
    private String deviceName;
}
