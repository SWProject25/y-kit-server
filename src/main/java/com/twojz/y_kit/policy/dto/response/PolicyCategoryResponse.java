package com.twojz.y_kit.policy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCategoryResponse {
    private Long id;
    private String name;
    private Integer level;
    private Long parentId;
    private Boolean isActive;
}
