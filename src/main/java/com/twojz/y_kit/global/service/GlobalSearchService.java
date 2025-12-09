package com.twojz.y_kit.global.service;

import com.twojz.y_kit.community.dto.response.CommunityListResponse;
import com.twojz.y_kit.community.service.CommunityFindService;
import com.twojz.y_kit.global.dto.GlobalSearchResponse;
import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.global.dto.SearchCategory;
import com.twojz.y_kit.group.dto.response.GroupPurchaseListResponse;
import com.twojz.y_kit.group.service.GroupPurchaseFindService;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.hotdeal.service.HotDealFindService;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.service.PolicyFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchService {
    private final CommunityFindService communityFindService;
    private final HotDealFindService hotDealFindService;
    private final GroupPurchaseFindService groupPurchaseFindService;
    private final PolicyFindService policyFindService;

    /**
     * 통합 검색 - 전체 카테고리
     */
    public GlobalSearchResponse searchAll(String keyword, Long userId, Pageable pageable) {
        List<String> extractedKeywords = extractKeywords(keyword);

        PageResponse<CommunityListResponse> communities =
                communityFindService.searchCommunities(null, keyword, userId, pageable);

        PageResponse<HotDealListResponse> hotDeals =
                hotDealFindService.searchHotDeals(null, null, keyword, userId, pageable);

        PageResponse<GroupPurchaseListResponse> groupPurchases =
                groupPurchaseFindService.searchGroupPurchases(keyword, null, null, null, userId, pageable);

        PageResponse<PolicyListResponse> policies =
                policyFindService.searchPolicies(keyword, null, null, userId, pageable);

        return GlobalSearchResponse.builder()
                .keyword(keyword)
                .extractedKeywords(extractedKeywords)
                .communities(communities)
                .hotDeals(hotDeals)
                .groupPurchases(groupPurchases)
                .policies(policies)
                .totalCount(
                        communities.getTotalElements() +
                                hotDeals.getTotalElements() +
                                groupPurchases.getTotalElements() +
                                policies.getTotalElements()
                )
                .build();
    }

    /**
     * 통합 검색 - 특정 카테고리만
     */
    public GlobalSearchResponse searchByCategory(
            String keyword,
            SearchCategory category,
            Long userId,
            Pageable pageable
    ) {
        List<String> extractedKeywords = extractKeywords(keyword);

        GlobalSearchResponse.GlobalSearchResponseBuilder builder =
                GlobalSearchResponse.builder()
                        .keyword(keyword)
                        .extractedKeywords(extractedKeywords);

        long totalCount = 0;

        switch (category) {
            case COMMUNITY -> {
                PageResponse<CommunityListResponse> communities =
                        communityFindService.searchCommunities(null, keyword, userId, pageable);
                builder.communities(communities);
                totalCount = communities.getTotalElements();
            }
            case HOTDEAL -> {
                PageResponse<HotDealListResponse> hotDeals =
                        hotDealFindService.searchHotDeals(null, null, keyword, userId, pageable);
                builder.hotDeals(hotDeals);
                totalCount = hotDeals.getTotalElements();
            }
            case GROUP_PURCHASE -> {
                PageResponse<GroupPurchaseListResponse> groupPurchases =
                        groupPurchaseFindService.searchGroupPurchases(keyword, null, null, null, userId, pageable);
                builder.groupPurchases(groupPurchases);
                totalCount = groupPurchases.getTotalElements();
            }
            case POLICY -> {
                PageResponse<PolicyListResponse> policies =
                        policyFindService.searchPolicies(keyword, null, null, userId, pageable);
                builder.policies(policies);
                totalCount = policies.getTotalElements();
            }
        }

        return builder.totalCount(totalCount).build();
    }

    /**
     * 통합 검색 - 미리보기 (각 카테고리당 5개씩)
     */
    public GlobalSearchResponse searchPreview(String keyword, Long userId) {
        List<String> extractedKeywords = extractKeywords(keyword);

        Pageable pageable = PageRequest.of(0, 5);

        PageResponse<CommunityListResponse> communities =
                communityFindService.searchCommunities(null, keyword, userId, pageable);

        PageResponse<HotDealListResponse> hotDeals =
                hotDealFindService.searchHotDeals(null, null, keyword, userId, pageable);

        PageResponse<GroupPurchaseListResponse> groupPurchases =
                groupPurchaseFindService.searchGroupPurchases(keyword, null, null, null, userId, pageable);

        PageResponse<PolicyListResponse> policies =
                policyFindService.searchPolicies(keyword, null, null, userId, pageable);

        return GlobalSearchResponse.builder()
                .keyword(keyword)
                .extractedKeywords(extractedKeywords)
                .communities(communities)
                .hotDeals(hotDeals)
                .groupPurchases(groupPurchases)
                .policies(policies)
                .totalCount(
                        communities.getTotalElements() +
                                hotDeals.getTotalElements() +
                                groupPurchases.getTotalElements() +
                                policies.getTotalElements()
                )
                .build();
    }

    /**
     * 형태소 분석을 통해 의미있는 키워드 추출
     */
    private List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        try {
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
            Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
            return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens)
                    .stream()
                    .filter(keyword -> keyword.length() > 1)
                    .distinct()
                    .limit(5)
                    .toList();

        } catch (Exception e) {
            log.error("형태소 분석 실패: {}", text, e);
            return List.of(text);
        }
    }
}