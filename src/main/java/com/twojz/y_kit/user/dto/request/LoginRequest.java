package com.twojz.y_kit.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Schema(description = "이메일", example = "test@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    private String password;
}
