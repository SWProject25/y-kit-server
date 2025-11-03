package com.twojz.y_kit.policy.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책 취업 요건 코드
 */
@Getter
@RequiredArgsConstructor
public enum EmploymentStatus {
    CODE_13001("13001", "재직자"),
    CODE_13002("13002", "자영업자"),
    CODE_13003("13003", "미취업자"),
    CODE_13004("13004", "프리랜서"),
    CODE_13005("13005", "일용근로자"),
    CODE_13006("13006", "(예비)창업자"),
    CODE_13007("13007", "단기근로자"),
    CODE_13008("13008", "영농종사자"),
    CODE_13009("13009", "기타"),
    CODE_13010("13010", "제한없음");

    private final String code;
    private final String description;

    public static EmploymentStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (EmploymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
