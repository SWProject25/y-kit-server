package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ProfileCompleteRequest {
    @Schema(description = "이름")
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
}