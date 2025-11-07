package com.twojz.y_kit.policy.domain.enumType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책승인상태 구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum PolicyApprovalStatus {
    CODE_44001("0044001", "신청"),
    CODE_44002("0044002", "승인"),
    CODE_44003("0044003", "반려"),
    CODE_44004("0044004", "임시저장");

    private final String code;
    private final String description;

    public static PolicyApprovalStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (PolicyApprovalStatus type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
