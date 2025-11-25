package com.twojz.y_kit.policy.domain.enumType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결혼 상태 코드
 */
@Getter
@RequiredArgsConstructor
public enum MaritalStatus {
    CODE_55001("55001", "기혼"),
    CODE_55002("55002", "미혼"),
    CODE_55003("55003", "제한없음");

    private final String code;
    private final String description;

    public static MaritalStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (MaritalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}