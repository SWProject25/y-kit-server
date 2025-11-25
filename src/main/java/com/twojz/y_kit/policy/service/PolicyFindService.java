package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import java.time.LocalDate;
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
     * 추천 정책 조회 - Service
     */
    public PageResponse<PolicyListResponse> getRecommendedPolicies(
            Integer age,
            String regionCode,
            Long categoryId,
            Pageable pageable
    ) {
        LocalDate today = LocalDate.now();

        // DB에서 나이 + 지역 + 신청 가능 + 카테고리 조건까지 모두 적용
        Page<PolicyEntity> policyPage = policyRepository.findRecommendedWithCategory(
                age, regionCode, today, categoryId, pageable
        );

        List<PolicyEntity> policies = policyPage.getContent();

        // N+1 방지: 컬렉션 일괄 조회
        if (!policies.isEmpty()) {
            policyRepository.findWithCategories(policies);
            policyRepository.findWithKeywords(policies);
            policyRepository.findWithRegions(policies);
        }

        // DTO로 변환
        List<PolicyListResponse> content = policies.stream()
                .map(policyMapper::toListResponse)
                .collect(Collectors.toList());

        // PageImpl로 새 페이지 생성 (totalElements는 DB에서 계산된 값 사용)
        Page<PolicyListResponse> mappedPage = new PageImpl<>(content, pageable, policyPage.getTotalElements());

        return new PageResponse<>(mappedPage);
    }

    /**
     * 인기 정책 조회
     */
    public PageResponse<PolicyListResponse> getPopularPolicies(String sortBy, Pageable pageable) {
        Page<PolicyEntity> policyPage;

        if ("bookmarkCount".equals(sortBy)) {
            policyPage = policyRepository.findPopularByBookmarkCount(pageable);
        } else {
            policyPage = policyRepository.findPopularByViewCount(pageable);
        }

        // 컬렉션 일괄 조회
        List<PolicyEntity> policies = policyPage.getContent();
        fetchCollections(policies);

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
        fetchCollections(policies);

        Page<PolicyListResponse> mappedPage = policyPage.map(policyMapper::toListResponse);

        return new PageResponse<>(mappedPage);
    }

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
