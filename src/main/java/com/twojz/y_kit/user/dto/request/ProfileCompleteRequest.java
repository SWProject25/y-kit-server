package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.user.entity.Gender;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ProfileCompleteRequest {
    private String name;
    private String nickName;
    private LocalDate birthDate;
    private Gender gender;
    private String region;
    private EmploymentStatus employmentStatus;
    private EducationLevel educationLevel;
    private MajorField major;
}