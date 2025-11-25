package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

/**
 * 정책 상세 응답 DTO
 */
@Getter
@Builder
@Schema(description = "정책 상세 응답")
public class PolicyDetailResponse {
    @Schema(description = "정책 기본 정보")
    private PolicyBasicInfo basicInfo;

    @Schema(description = "정책 상세 정보")
    private PolicyDetail detail;

    @Schema(description = "신청 정보")
    private PolicyApplication application;

    @Schema(description = "자격 요건")
    private PolicyQualification qualification;

    @Schema(description = "제출 서류")
    private PolicyDocument document;

    @Schema(description = "카테고리 정보")
    private List<CategoryInfo> categories;

    @Schema(description = "키워드 목록")
    private List<String> keywords;

    @Schema(description = "적용 지역 목록")
    private List<RegionInfo> regions;

    @Schema(description = "AI 분석 정보")
    private AiAnalysisInfo aiAnalysis;

    public static List<String> toKeywords(List<PolicyKeywordMapping> mappings) {
        if (mappings == null) {
            return new ArrayList<>();
        }

        return mappings.stream()
                .map(mapping -> mapping.getKeyword().getKeyword())
                .collect(Collectors.toList());
    }
}
