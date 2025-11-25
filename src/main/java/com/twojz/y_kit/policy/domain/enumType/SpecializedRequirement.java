package com.twojz.y_kit.policy.domain.enumType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책 특화 요건 코드
 */
@Getter
@RequiredArgsConstructor
public enum SpecializedRequirement {
    CODE_14001("14001", "중소기업"),
    CODE_14002("14002", "여성"),
    CODE_14003("14003", "기초생활수급자"),
    CODE_14004("14004", "한부모가정"),
    CODE_14005("14005", "장애인"),
    CODE_14006("14006", "농업인"),
    CODE_14007("14007", "군인"),
    CODE_14008("14008", "지역인재"),
    CODE_14009("14009", "기타"),
    CODE_14010("14010", "제한없음");

    private final String code;
    private final String description;

    public static SpecializedRequirement fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (SpecializedRequirement req : values()) {
            if (req.code.equals(code)) {
                return req;
            }
        }
        return null;
    }
}

