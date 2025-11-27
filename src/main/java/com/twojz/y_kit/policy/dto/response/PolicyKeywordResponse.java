package com.twojz.y_kit.policy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyKeywordResponse {
    private Long id;
    private String keyword;
    private Integer usageCount;
}
