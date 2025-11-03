package com.twojz.y_kit.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신청기간 구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationPeriodType {
    CODE_57001("57001", "특정기간"),
    CODE_57002("57002", "상시"),
    CODE_57003("57003", "마감");

    private final String code;
    private final String description;

    public static ApplicationPeriodType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ApplicationPeriodType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}