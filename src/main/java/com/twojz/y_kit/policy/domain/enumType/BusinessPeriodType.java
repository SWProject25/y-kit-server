package com.twojz.y_kit.policy.domain.enumType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사업기간 구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum BusinessPeriodType {
    CODE_56001("0056001", "특정기간"),
    CODE_56002("0056002", "기타");

    private final String code;
    private final String description;

    public static BusinessPeriodType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (BusinessPeriodType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
