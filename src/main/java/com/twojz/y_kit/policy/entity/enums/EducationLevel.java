package com.twojz.y_kit.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책 학력 요건 코드
 */
@Getter
@RequiredArgsConstructor
public enum EducationLevel {
    CODE_49001("49001", "고졸 미만"),
    CODE_49002("49002", "고교 재학"),
    CODE_49003("49003", "고졸 예정"),
    CODE_49004("49004", "고교 졸업"),
    CODE_49005("49005", "대학 재학"),
    CODE_49006("49006", "대졸 예정"),
    CODE_49007("49007", "대학 졸업"),
    CODE_49008("49008", "석·박사"),
    CODE_49009("49009", "기타"),
    CODE_49010("49010", "제한없음");

    private final String code;
    private final String description;

    public static EducationLevel fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (EducationLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return null;
    }
}