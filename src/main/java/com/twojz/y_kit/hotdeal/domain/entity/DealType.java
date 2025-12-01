package com.twojz.y_kit.hotdeal.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DealType {
    DISCOUNT("할인"),
    SALE("세일"),
    EVENT("이벤트"),
    ETC("기타");

    private final String value;
}
