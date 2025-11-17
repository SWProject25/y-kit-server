package com.twojz.y_kit.hotdeal.domain.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DealType {
    DISCOUNT("할인"),
    SALE("세일"),
    EVENT("이벤트");

    private final String value;
}
