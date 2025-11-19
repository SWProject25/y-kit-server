package com.twojz.y_kit.policy.service;

import static org.apache.commons.lang3.StringUtils.truncate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.dto.request.PolicySearchRequest;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyFindService {
    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;

    /**
     * 정책 목록 조회 - 기본
     */
    public PageResponse<PolicyListResponse> getPolicyList(Pageable pageable) {
        Page<PolicyEntity> policyPage = policyRepository.findAllInfo(pageable);

        List<PolicyEntity> policies = policyPage.getContent();
        fetchCollections(policies);

        Page<PolicyListResponse> mappedPage = policyPage.map(policyMapper::toListResponse);
        return new PageResponse<>(mappedPage);
    }

    /**
     * 정책 상세 조회 (ID)
     */
    @Transactional
    public PolicyDetailResponse getPolicyDetail(Long policyId) {
        PolicyEntity policy = policyRepository.findByIdWithDetails(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다. ID: " + policyId));

        fetchCollectionsForDetail(policy);

        // 조회수 증가
        policy.increaseViewCount();

        return policyMapper.toDetailResponse(policy);
    }

    /**
     * 추천 정책 조회
     */
    public PageResponse<PolicyListResponse> getRecommendedPolicies(
            Integer age,
            String regionCode,
            Long categoryId,
            Pageable pageable
    ) {
        LocalDate today = LocalDate.now();
        Page<PolicyEntity> policyPage = policyRepository.findRecommended(age, regionCode, today, pageable);

        // 카테고리 필터링 (content 기준으로 필터)
        List<PolicyEntity> policies = policyPage.getContent();
        if (categoryId != null) {
            policies = policies.stream()
                    .filter(p -> p.getCategoryMappings() != null &&
                            p.getCategoryMappings().stream()
                                    .anyMatch(cm -> cm.getCategory().getId().equals(categoryId)))
                    .collect(Collectors.toList());
        }

        // 컬렉션 일괄 조회
        if (!policies.isEmpty()) {
            policyRepository.findWithCategories(policies);
            policyRepository.findWithKeywords(policies);
            policyRepository.findWithRegions(policies);
        }

        // content -> DTO
        List<PolicyListResponse> content = policies.stream()
                .map(policyMapper::toListResponse)
                .collect(Collectors.toList());

        // 카테고리 필터로 content가 변경되었으므로 PageImpl로 새 Page 생성 (총개수는 필터 후 사이즈)
        Page<PolicyListResponse> mappedPage = new PageImpl<>(content, pageable, content.size());

        return new PageResponse<>(mappedPage);
    }

    /**
     * 인기 정책 조회
     */
    public PageResponse<PolicyListResponse> getPopularPolicies(String sortBy, Pageable pageable) {
        Page<PolicyEntity> policyPage = switch (sortBy) {
            case "bookmarkCount" -> policyRepository.findPopularByBookmarkCount(pageable);
            case "applicationCount", "viewCount" -> policyRepository.findPopularByViewCount(pageable);
            default -> policyRepository.findPopularByViewCount(pageable);
        };

        // 컬렉션 일괄 조회
        List<PolicyEntity> policies = policyPage.getContent();
        if (!policies.isEmpty()) {
            policyRepository.findWithCategories(policies);
            policyRepository.findWithKeywords(policies);
            policyRepository.findWithRegions(policies);
        }

        Page<PolicyListResponse> mappedPage = policyPage.map(policyMapper::toListResponse);

        return new PageResponse<>(mappedPage);
    }

    /**
     * 마감 임박 정책 조회
     */
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<PolicyEntity> policyPage = policyRepository.findDeadlineSoon(today, pageable);

        // 컬렉션 일괄 조회
        List<PolicyEntity> policies = policyPage.getContent();
        if (!policies.isEmpty()) {
            policyRepository.findWithCategories(policies);
            policyRepository.findWithKeywords(policies);
            policyRepository.findWithRegions(policies);
        }

        Page<PolicyListResponse> mappedPage = policyPage.map(policyMapper::toListResponse);

        return new PageResponse<>(mappedPage);
    }


    /**
     * 검색 조건 정책 조회
     */
    /*public PageResponse<PolicyListResponse> getSearchPolicies(PolicySearchRequest request, Pageable pageable) {
        Page<PolicyEntity> policyPage = fetchPoliciesWithFilters(request, pageable);

        List<PolicyEntity> policies = policyPage.getContent();
        fetchCollections(policies);

        Page<PolicyListResponse> mappedPage = policyPage.map(policyMapper::toListResponse);

        return new PageResponse<>(mappedPage);
    }

    private Page<PolicyEntity> fetchPoliciesWithFilters(PolicySearchRequest request, Pageable pageable) {
        // 복합 조건
        if (request.getKeyword() != null && request.getAge() != null) {
            return policyRepository.findByKeywordAndAge(request.getKeyword(), request.getAge(), pageable);
        }

        // 단일 조건
        if (request.getCategoryId() != null) {
            return policyRepository.findByCategoryId(request.getCategoryId(), pageable);
        }
        if (request.getKeyword() != null) {
            return policyRepository.findByKeyword(request.getKeyword(), pageable);
        }
        if (request.getRegionCode() != null) {
            return policyRepository.findByRegionCode(request.getRegionCode(), pageable);
        }
        if (request.getAge() != null) {
            return policyRepository.findByAge(request.getAge(), pageable);
        }
        if (Boolean.TRUE.equals(request.getIsApplicationAvailable())) {
            return policyRepository.findApplicationAvailable(LocalDate.now(), pageable);
        }

        // 기본: 전체 조회
        return policyRepository.findAllWithBasicInfo(pageable);
    }*/

    // 컬렉션 일괄 조회
    private void fetchCollections(List<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) return;

        policyRepository.findWithCategories(policies);
        policyRepository.findWithKeywords(policies);
        policyRepository.findWithRegions(policies);
    }

    // 컬렉션 조회
    private void fetchCollectionsForDetail(PolicyEntity policy) {
        List<PolicyEntity> singleList = List.of(policy);
        policyRepository.findWithCategories(singleList);
        policyRepository.findWithKeywords(singleList);
        policyRepository.findWithRegions(singleList);
    }
}
