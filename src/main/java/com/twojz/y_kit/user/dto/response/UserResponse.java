package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.policy.domain.enumType.EducationLevel;
import com.twojz.y_kit.policy.domain.enumType.EmploymentStatus;
import com.twojz.y_kit.policy.domain.enumType.MajorField;
import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.Gender;
import com.twojz.y_kit.user.entity.UserEntity;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private final Long id;
    private final String email;
    private final Role role;
    private final LoginProvider loginProvider;
    private final String name;
    private final String nickName;
    private final LocalDate birthDate;
    private final Gender gender;
    private final String region;
    private final EmploymentStatus employmentStatus;
    private final EducationLevel educationLevel;
    private final MajorField major;
    private final ProfileStatus profileStatus;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .loginProvider(user.getLoginProvider())
                .name(user.getName())
                .nickName(user.getNickName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .region(user.getRegion() != null ? user.getRegion().getName() : null)
                .employmentStatus(user.getEmploymentStatus())
                .educationLevel(user.getEducationLevel())
                .major(user.getMajor())
                .profileStatus(user.getProfileStatus())
                .build();
    }
}
