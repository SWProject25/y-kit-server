package com.twojz.y_kit.user.dto.response;

import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.Gender;
import com.twojz.y_kit.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    private final Integer age;
    private final Gender gender;
    private final Long regionId;

    @Builder
    public UserResponse(Long id, String email, String name, Role role, Integer age, Gender gender, Long regionId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.age = age;
        this.gender = gender;
        this.regionId = regionId;
    }


    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .age(user.getAge())
                .gender(user.getGender())
                .regionId(user.getRegionId())
                .build();
    }
}