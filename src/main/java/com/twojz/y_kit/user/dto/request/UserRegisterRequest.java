package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequest {
    private Gender gender;
    private Long regionId;
    private Integer age;
    private String email;
    private String password;
    private String name;
}

