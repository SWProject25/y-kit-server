package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HotDealSearchFilter {
    private String regionCode;
    private DealType dealType;
    private String keyword;

    public HotDealSearchFilter(String regionCode, DealType dealType, String keyword) {
        this.regionCode = regionCode;
        this.dealType = dealType;
        this.keyword = keyword;
    }
}
