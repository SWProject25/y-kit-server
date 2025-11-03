package com.twojz.y_kit.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책 전공 요건 코드
 */
@Getter
@RequiredArgsConstructor
public enum MajorField {
    CODE_11001("11001", "인문계열"),
    CODE_11002("11002", "사회계열"),
    CODE_11003("11003", "상경계열"),
    CODE_11004("11004", "이학계열"),
    CODE_11005("11005", "공학계열"),
    CODE_11006("11006", "예체능계열"),
    CODE_11007("11007", "농산업계열"),
    CODE_11008("11008", "기타"),
    CODE_11009("11009", "제한없음");

    private final String code;
    private final String description;

    public static MajorField fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (MajorField field : values()) {
            if (field.code.equals(code)) {
                return field;
            }
        }
        return null;
    }
}
