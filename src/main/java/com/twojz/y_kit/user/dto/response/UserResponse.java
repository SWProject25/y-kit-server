package com.twojz.y_kit.user.dto.response;

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
    private final String name;
    private final Role role;
    private final LocalDate birthDate;
    private final Gender gender;
    private final String region;
    private ProfileStatus profileStatus;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .region(user.getRegion() != null ? user.getRegion().getName() : null)
                .profileStatus(user.getProfileStatus())
                .build();
    }
}