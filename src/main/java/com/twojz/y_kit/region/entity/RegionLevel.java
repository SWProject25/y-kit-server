package com.twojz.y_kit.region.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegionLevel {
    SIDO(1, "시/도"),
    SIGUNGU(2, "시군구"),
    DONG(3, "읍면동"),
    REE(4, "리");

    private final int level;
    private final String description;
}
