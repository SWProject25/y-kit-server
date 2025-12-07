package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
public class LocalSignUpRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Schema(description = "닉네임")
    private String nickName;

    @Schema(description = "생년월일", example = "1998-03-15")
    private LocalDate birthDate;

    @Schema(description = "성별 (MAN / WOMAN)", example = "MAN")
    private Gender gender;

    @Schema(description = "사용자 지역 코드", example = "11010")
    private String regionCode;

    @Schema(description = "현재 직업 상태")
    private EmploymentStatus employmentStatus;

    @Schema(description = "학력")
    private EducationLevel educationLevel;

    @Schema(description = "전공")
    private MajorField major;

    private String deviceToken;
    private String deviceName;
}

