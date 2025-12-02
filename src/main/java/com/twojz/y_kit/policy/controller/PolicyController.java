package com.twojz.y_kit.policy.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.dto.response.PolicyCategoryResponse;
import com.twojz.y_kit.policy.dto.response.PolicyComparisonResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyKeywordResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.service.PolicyComparisonService;
import com.twojz.y_kit.policy.service.PolicyFindService;
import com.twojz.y_kit.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "정책 조회 API")
public class PolicyController {
    private final PolicyFindService policyFindService;
    private final PolicyComparisonService policyComparisonService;

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

    /**
     * 내 맞춤 추천 정책 조회 (로그인한 사용자 기반)
     */
    @GetMapping("/recommended/me")
    @Operation(summary = "내 맞춤 추천 정책 조회", description = "로그인한 사용자의 나이와 지역을 기반으로 맞춤 추천 정책을 조회합니다.")
    public List<PolicyListResponse> getMyRecommendedPolicies(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int limit
    ) {
        Long userId = extractUserId(authentication);
        return policyFindService.getMyRecommendedPolicies(userId, limit);
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
    @Operation(summary = "마감 임박 정책 조회")
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(
            @ParameterObject Pageable pageable
    ) {
        return policyFindService.getDeadlineSoonPolicies(pageable);
    }

    /**
     * 정책 검색 및 필터링
     */
    @GetMapping("/search")
    @Operation(summary = "정책 검색 및 필터링")
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
    @Operation(summary = "정책 카테고리 목록 조회")
    public List<PolicyCategoryResponse> getPolicyCategories() {
        return policyFindService.getAllCategories();
    }

    /**
     * 정책 키워드 목록 조회
     */
    @GetMapping("/keywords")
    @Operation(summary = "정책 키워드 목록 조회")
    public List<PolicyKeywordResponse> getPolicyKeywords() {
        return policyFindService.getAllKeywords();
    }

    /**
     * AI기반 정책 비교
     */
    @PostMapping("/compare")
    @Operation(summary = "정책 비교")
    public ResponseEntity<PolicyComparisonResponse> comparePolicies(
            Authentication authentication,
            @RequestBody List<Long> policyIds
    ) {
        Long userId = extractUserId(authentication);
        PolicyComparisonResponse response = policyComparisonService.comparePolicies(userId, policyIds);

        return ResponseEntity.ok(response);
    }


    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 사용자 정보입니다.", e);
        }
    }

}
