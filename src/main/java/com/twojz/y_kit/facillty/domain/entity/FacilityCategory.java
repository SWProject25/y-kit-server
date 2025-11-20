package com.twojz.y_kit.facillty.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FacilityCategory {
    CULTURE_LODGE("010000", "문화·숙박"),
    MEETING_ROOM("010100", "회의실"),
    LECTURE_HALL("010200", "강의실·강당"),
    SPORTS("010500", "체육시설"),
    PARKING("010700", "주차장"),
    GOODS("020000", "물품"),
    LAB_EQUIP("030000", "연구·실험장비"),
    EDUCATION("040000", "교육·강좌");

    private final String code;
    private final String name;

    public static FacilityCategory fromCode(String code) {
        for (FacilityCategory c : values()) {
            if (c.code.equals(code)) return c;
        }
        return null;
    }
}