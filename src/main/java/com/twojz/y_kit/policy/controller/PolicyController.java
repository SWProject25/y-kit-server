package com.twojz.y_kit.policy.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.dto.response.PolicyCategoryResponse;
import com.twojz.y_kit.policy.dto.response.PolicyComparisonResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyKeywordResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.service.PolicyComparisonService;
import com.twojz.y_kit.policy.service.PolicyFindService;
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

    @GetMapping
    @Operation(summary = "정책 목록 조회", description = "로그인 시 북마크 여부 포함, 비로그인 시 북마크는 false")
    public PageResponse<PolicyListResponse> getPolicies(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.getPolicyList(userId, pageable);
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "정책 상세 조회", description = "로그인 시 북마크 여부 포함, 비로그인 시 북마크는 false")
    public PolicyDetailResponse getPolicyDetail(
            Authentication authentication,
            @PathVariable Long policyId
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.getPolicyDetail(userId, policyId);
    }

    @GetMapping("/search")
    @Operation(summary = "정책 검색 및 필터링 (형태소 분석 지원)",
            description = "키워드, 카테고리, 키워드 ID로 정책을 검색합니다. 로그인 시 북마크 여부 포함.")
    public PageResponse<PolicyListResponse> searchPolicies(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> keywordIds,
            @ParameterObject Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.searchPolicies(keyword, categoryIds, keywordIds, userId, pageable);
    }

    @GetMapping("/recommended")
    @Operation(summary = "사용자 맞춤 추천 정책 조회", description = "사용자 정보를 이용해 맞춤형 정책을 추천합니다.")
    public PageResponse<PolicyListResponse> getRecommendedPolicies(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.getRecommendedPoliciesByUser(userId, pageable);
    }

    @GetMapping("/similar")
    @Operation(summary = "유사 정책 추천 (로그인 필수)", description = "조회한 정책과 유사한 정책을 조회합니다.")
    public List<PolicyListResponse> getSimilarPolicies(
            Authentication authentication,
            @RequestParam Long policyId,
            @RequestParam(defaultValue = "3") int limit
    ) {
        Long userId = extractUserId(authentication);
        return policyFindService.getSimilarPolicies(policyId, userId, limit);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 정책 조회", description = "조회수, 북마크 수 등을 기준으로 인기 정책을 조회합니다. 로그인 시 북마크 여부 포함.")
    public PageResponse<PolicyListResponse> getPopularPolicies(
            Authentication authentication,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @ParameterObject Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.getPopularPolicies(sortBy, userId, pageable);
    }

    @GetMapping("/deadline")
    @Operation(summary = "마감 임박 정책 조회", description = "신청 마감일이 임박한 정책을 조회합니다. 로그인 시 북마크 여부 포함.")
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return policyFindService.getDeadlineSoonPolicies(userId, pageable);
    }

    @GetMapping("/categories")
    @Operation(summary = "정책 카테고리 목록 조회")
    public List<PolicyCategoryResponse> getPolicyCategories() {
        return policyFindService.getAllCategories();
    }

    @GetMapping("/keywords")
    @Operation(summary = "정책 키워드 목록 조회", description = "사용 빈도가 높은 상위 50개 키워드를 조회합니다.")
    public List<PolicyKeywordResponse> getPolicyKeywords() {
        return policyFindService.getAllKeywords();
    }

    @PostMapping("/compare")
    @Operation(summary = "AI 기반 정책 비교 (로그인 필수)", description = "여러 정책을 AI로 비교 분석합니다.")
    public ResponseEntity<PolicyComparisonResponse> comparePolicies(
            Authentication authentication,
            @RequestBody List<Long> policyIds
    ) {
        Long userId = extractUserId(authentication);
        PolicyComparisonResponse response = policyComparisonService.comparePolicies(userId, policyIds);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 필수 - userId 추출 (로그인 안되어 있으면 예외 발생)
     */
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

    /**
     * 로그인 선택 - userId 추출 (로그인 안되어 있으면 null 반환)
     */
    private Long extractUserIdOrNull(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}