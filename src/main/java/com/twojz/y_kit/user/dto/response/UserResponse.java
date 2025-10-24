package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.Gender;
import com.twojz.y_kit.user.entity.UserEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final Role role;
    private final Integer age;
    private final Gender gender;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .age(user.getAge())
                .gender(user.getGender())
                .build();
    }
}