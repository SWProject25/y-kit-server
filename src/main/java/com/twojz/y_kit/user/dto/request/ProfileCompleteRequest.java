package com.twojz.y_kit.user.dto.request;

import com.twojz.y_kit.user.entity.Gender;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ProfileCompleteRequest{
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String region;
}