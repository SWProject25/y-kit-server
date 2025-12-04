package com.twojz.y_kit.global.dto;

import com.twojz.y_kit.community.dto.response.CommunityListResponse;
import com.twojz.y_kit.group.dto.response.GroupPurchaseListResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "전체 통합 검색 응답 DTO")
public class GlobalSearchResponse {
    @Schema(description = "검색 키워드")
    private String keyword;

    @Schema(description = "형태소 분석으로 추출된 키워드 리스트")
    private List<String> extractedKeywords;

    @Schema(description = "정책 검색 결과")
    private PageResponse<PolicyListResponse> policies;

    @Schema(description = "핫딜 검색 결과")
    private PageResponse<HotDealListResponse> hotDeals;

    @Schema(description = "공동구매 검색 결과")
    private PageResponse<GroupPurchaseListResponse> groupPurchases;

    @Schema(description = "커뮤니티 검색 결과")
    private PageResponse<CommunityListResponse> communities;

    @Schema(description = "전체 검색 결과 수")
    private Long totalCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCount {
        private long communityCount;
        private long hotDealCount;
        private long groupPurchaseCount;
        private long policyCount;
        private long totalCount;
    }

    /**
     * 카테고리별 개수 정보 반환
     */
    public CategoryCount getCategoryCount() {
        long communityCount = communities != null ? communities.getTotalElements() : 0;
        long hotDealCount = hotDeals != null ? hotDeals.getTotalElements() : 0;
        long groupPurchaseCount = groupPurchases != null ? groupPurchases.getTotalElements() : 0;
        long policyCount = policies != null ? policies.getTotalElements() : 0;

        return CategoryCount.builder()
                .communityCount(communityCount)
                .hotDealCount(hotDealCount)
                .groupPurchaseCount(groupPurchaseCount)
                .policyCount(policyCount)
                .totalCount(communityCount + hotDealCount + groupPurchaseCount + policyCount)
                .build();
    }
}
