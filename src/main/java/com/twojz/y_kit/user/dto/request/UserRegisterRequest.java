package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;

public class UserRegisterRequest {
    private Gender gender;
    private Long regionId;
    private Integer age;
    private String email;
    private String password;
    private String name;

    public UserRegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public Long getRegionId() {
        return regionId;
    }

    public Integer getAge() {
        return age;
    }
}

