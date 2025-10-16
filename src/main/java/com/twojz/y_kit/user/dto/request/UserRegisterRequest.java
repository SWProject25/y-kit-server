package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;
import lombok.Getter;

@Getter
public class UserRegisterRequest {
    private String email;
    private String password;
    private String name;
    private Integer age;
    private Gender gender;
}

