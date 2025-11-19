package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "카테고리 정보")
public class CategoryInfo {
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "일자리")
    private String categoryName;

    @Schema(description = "레벨 (1: 대분류, 2: 중분류)", example = "1")
    private Integer level;

    public static List<CategoryInfo> from(List<PolicyCategoryMapping> mappings) {
        return mappings.stream()
                .map(m -> CategoryInfo.builder()
                        .categoryId(m.getCategory().getId())
                        .categoryName(m.getCategory().getName())
                        .level(m.getCategory().getLevel())
                        .build())
                .toList();
    }
}
