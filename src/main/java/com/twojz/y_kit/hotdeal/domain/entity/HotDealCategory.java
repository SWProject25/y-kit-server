package com.twojz.y_kit.hotdeal.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotDealCategory {
    FOOD("식품"),
    ELECTRONICS("전자제품"),
    FASHION("패션"),
    LIVING("생활용품"),
    BEAUTY("뷰티"),
    BOOK("도서"),
    SPORTS("스포츠");

    private final String description;
}
