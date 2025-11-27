package com.twojz.y_kit.policy.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.dto.response.PolicyCategoryResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyKeywordResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.service.PolicyFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "정책 조회 API")
public class PolicyController {
    private final PolicyFindService policyFindService;

    // -----------------------------------------
    // 1. 정책 목록 조회
    // -----------------------------------------
    @GetMapping
    public PageResponse<PolicyListResponse> getPolicies(@ParameterObject Pageable pageable) {
        return policyFindService.getPolicyList(pageable);
    }

    // -----------------------------------------
    // 2. 정책 상세 조회 (ID)
    // -----------------------------------------
    @GetMapping("/{policyId}")
    @Operation(summary = "정책 상세 조회(ID)", description = "정책 ID를 기반으로 상세 정보를 조회합니다.")
    public PolicyDetailResponse getPolicyDetail(@PathVariable Long policyId) {
        return policyFindService.getPolicyDetail(policyId);
    }


    // -----------------------------------------
    // 3. 추천 정책 조회
    // -----------------------------------------
    @GetMapping("/recommended")
    @Operation(summary = "추천 정책 조회", description = "나이, 지역 코드, 카테고리를 기반으로 추천 정책을 조회합니다.")
    public PageResponse<PolicyListResponse> getRecommendedPolicies(
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Long categoryId,
            @ParameterObject Pageable pageable
    ) {
        return policyFindService.getRecommendedPolicies(age, regionCode, categoryId, pageable);
    }

    // -----------------------------------------
    // 4. 인기 정책 조회
    // -----------------------------------------
    @GetMapping("/popular")
    @Operation(summary = "인기 정책 조회", description = "조회수, 북마크 수 등을 기준으로 인기 정책을 조회합니다.")
    public PageResponse<PolicyListResponse> getPopularPolicies(
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @ParameterObject Pageable pageable
    ) {
        return policyFindService.getPopularPolicies(sortBy, pageable);
    }

    // -----------------------------------------
    // 5. 마감 임박 정책 조회
    // -----------------------------------------
    @GetMapping("/deadline")
    @Operation(summary = "마감 임박 정책 조회", description = "마감일이 가까운 정책들을 조회합니다.")
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(
            @ParameterObject Pageable pageable
    ) {
        return policyFindService.getDeadlineSoonPolicies(pageable);
    }

    /**
     * 정책 검색 및 필터링
     */
    @GetMapping("/search")
    @Operation(summary = "정책 검색 및 필터링",
            description = "키워드, 카테고리, 키워드 태그로 정책을 검색합니다.")
    public PageResponse<PolicyListResponse> searchPolicies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> keywordIds,
            @ParameterObject Pageable pageable
    ) {
        return policyFindService.searchPolicies(keyword, categoryIds, keywordIds, pageable);
    }

    /**
     * 정책 카테고리 목록 조회
     */
    @GetMapping("/categories")
    @Operation(summary = "정책 카테고리 목록 조회",
            description = "모든 활성화된 정책 카테고리를 조회합니다.")
    public List<PolicyCategoryResponse> getPolicyCategories() {
        return policyFindService.getAllCategories();
    }

    /**
     * 정책 키워드 목록 조회
     */
    @GetMapping("/keywords")
    @Operation(summary = "정책 키워드 목록 조회",
            description = "사용빈도가 높은 상위 50개 키워드를 조회합니다.")
    public List<PolicyKeywordResponse> getPolicyKeywords() {
        return policyFindService.getAllKeywords();
    }

}
