package com.twojz.y_kit.global.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchCategory {
    COMMUNITY("커뮤니티"),
    HOTDEAL("핫딜"),
    GROUP_PURCHASE("공동구매"),
    POLICY("정책정보"),
    ALL("전체");

    private final String displayName;
}
