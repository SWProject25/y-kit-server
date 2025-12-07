package com.twojz.y_kit.group.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupPurchaseCategory {
    FOOD("식품"),
    ELECTRONICS("전자제품"),
    FASHION("패션/뷰티"),
    LIVING("생활용품"),
    BOOK("도서"),
    ETC("기타");

    private final String description;
}
