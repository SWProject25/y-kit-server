package com.twojz.y_kit.policy.domain.enumType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정책제공 방법 코드
 */
@Getter
@RequiredArgsConstructor
public enum PolicyProvisionMethod {
    CODE_42001("0042001", "인프라 구축"),
    CODE_42002("0042002", "프로그램"),
    CODE_42003("0042003", "직접대출"),
    CODE_42004("0042004", "공공기관"),
    CODE_42005("0042005", "계약(위착운영)"),
    CODE_42006("0042006", "보조금"),
    CODE_42007("0042007", "대출보증"),
    CODE_42008("0042008", "공적보험"),
    CODE_42009("0042009", "조세지출"),
    CODE_42010("0042010", "바우처"),
    CODE_42011("0042011", "정보제공"),
    CODE_42012("0042012", "경제적 규제"),
    CODE_42013("0042013", "기타");


    private final String code;
    private final String description;

    public static PolicyProvisionMethod fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (PolicyProvisionMethod type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
