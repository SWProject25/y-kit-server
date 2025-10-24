package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;
import lombok.Getter;

@Getter
public class LocalSignUpRequest {
    private String email;
    private String password;
    private String name;
    private int age;
    private Gender gender;
}

