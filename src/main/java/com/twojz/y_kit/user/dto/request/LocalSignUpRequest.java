package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;
import java.time.LocalDate;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
public class LocalSignUpRequest {
    @Schema(description = "이메일", example = "test@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "생년월일", example = "1998-03-15")
    private LocalDate birthDate;

    @Schema(description = "성별 (MAN / WOMAN)", example = "MAN")
    private Gender gender;

    @Schema(description = "사용자 지역", example = "서울특별시")
    private String region;
}

