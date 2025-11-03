package com.twojz.y_kit.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소득 조건 구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum IncomeConditionType {
    CODE_43001("43001", "무관"),
    CODE_43002("43002", "연소득"),
    CODE_43003("43003", "기타");

    private final String code;
    private final String description;

    public static IncomeConditionType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (IncomeConditionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
