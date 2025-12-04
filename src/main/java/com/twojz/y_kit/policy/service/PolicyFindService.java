package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.dto.response.PolicyCategoryResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyKeywordResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.policy.repository.PolicyCategoryRepository;
import com.twojz.y_kit.policy.repository.PolicyKeywordRepository;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyFindService {
    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final PolicyKeywordRepository policyKeywordRepository;
    private final PolicyBookmarkRepository policyBookmarkRepository;
    private final UserFindService userFindService;

    public List<PolicyEntity> getPoliciesByIds(List<Long> ids) {
        return policyRepository.findAllById(ids);
    }

    public PolicyEntity getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));
    }

    public PageResponse<PolicyListResponse> getPolicyList(Long userId, Pageable pageable) {
        Page<PolicyEntity> policyPage = policyRepository.findAllInfo(pageable);
        return convertToPageResponse(policyPage, userId);
    }

    @Transactional
    public PolicyDetailResponse getPolicyDetail(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findByIdWithDetails(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다. ID: " + policyId));

        fetchCollectionsForDetail(policy);
        policy.increaseViewCount();

        boolean isBookmarked = false;
        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isBookmarked = policyBookmarkRepository.existsByPolicyAndUser(policy, user);
        }

        return policyMapper.toDetailResponse(policy, isBookmarked);
    }

    /**
     * 파라미터 기반 추천 정책 조회
     */
    public PageResponse<PolicyListResponse> getRecommendedPoliciesByParams(
            Integer age,
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        Page<PolicyEntity> policyPage = policyRepository.findRecommendedWithCategory(
                age, regionCode, pageable
        );

        List<PolicyEntity> policies = policyPage.getContent();

        if (!policies.isEmpty()) {
            policyRepository.findWithCategories(policies);
            policyRepository.findWithKeywords(policies);
            policyRepository.findWithRegions(policies);
        }

        Map<Long, Boolean> bookmarkMap = getBookmarkMap(policies, userId);

        List<PolicyListResponse> content = policies.stream()
                .map(policy -> policyMapper.toListResponse(
                        policy,
                        bookmarkMap.getOrDefault(policy.getId(), false)
                ))
                .collect(Collectors.toList());

        Page<PolicyListResponse> mappedPage = new PageImpl<>(content, pageable, policyPage.getTotalElements());
        return new PageResponse<>(mappedPage);
    }

    /**
     * 사용자 정보 기반 추천 정책 조회 (전체 프로필 활용)
     */
    public PageResponse<PolicyListResponse> getRecommendedPoliciesByUser(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);

        if (user.getProfileStatus() != ProfileStatus.COMPLETED) {
            throw new IllegalStateException("프로필 정보가 완료되지 않았습니다.");
        }

        Page<PolicyEntity> policyPolicy = policyRepository.findRecommendedWithProfile(
                user.calculateAge(),
                user.getRegion().getCode(),
                user.getEmploymentStatus(),
                user.getEducationLevel(),
                user.getMajor(),
                pageable
        );

        return convertToPageResponse(policyPolicy, userId);
    }

    /**
     * 인기 정책 조회
     */
    public PageResponse<PolicyListResponse> getPopularPolicies(String sortBy, Long userId, Pageable pageable) {
        Page<PolicyEntity> policyPage;

        if ("bookmarkCount".equals(sortBy)) {
            policyPage = policyRepository.findPopularByBookmarkCount(pageable);
        } else {
            policyPage = policyRepository.findPopularByViewCount(pageable);
        }

        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 마감 임박 정책 조회
     */
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<PolicyEntity> policyPage = policyRepository.findDeadlineSoon(today, pageable);
        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 모든 정책 카테고리 조회
     */
    public List<PolicyCategoryResponse> getAllCategories() {
        return policyCategoryRepository.findAllByIsActiveTrue()
                .stream()
                .map(category -> PolicyCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .level(category.getLevel())
                        .parentId(category.getParent() != null ? category.getParent().getId() : null)
                        .isActive(category.getIsActive())
                        .build())
                .toList();
    }

    /**
     * 모든 정책 키워드 조회 (사용빈도 높은 순 상위 50개)
     */
    public List<PolicyKeywordResponse> getAllKeywords() {
        return policyKeywordRepository.findTop50ByOrderByUsageCountDesc()
                .stream()
                .map(keyword -> PolicyKeywordResponse.builder()
                        .id(keyword.getId())
                        .keyword(keyword.getKeyword())
                        .usageCount(keyword.getUsageCount())
                        .build())
                .toList();
    }

    /**
     * 정책 검색 및 필터링 (형태소 분석 지원)
     * - categoryIds: PolicyCategoryEntity의 ID 리스트
     * - keywordIds: PolicyKeywordEntity의 ID 리스트
     * - keyword: 정책명(plcyNm)/설명(plcyExplnCn) 텍스트 검색 (형태소 분석 적용)
     */
    public PageResponse<PolicyListResponse> searchPolicies(
            String keyword,
            List<Long> categoryIds,
            List<Long> keywordIds,
            Long userId,
            Pageable pageable
    ) {
        List<String> extractedKeywords = (keyword != null && !keyword.isEmpty())
                ? extractKeywords(keyword)
                : List.of();

        if (!extractedKeywords.isEmpty()) {
            log.info("정책 검색 - 검색어: {}, 추출된 키워드: {}", keyword, extractedKeywords);
        }

        Page<PolicyEntity> policyPage = policyRepository.searchPoliciesUnified(
                categoryIds,
                keywordIds,
                getKeywordOrNull(extractedKeywords, 0),
                getKeywordOrNull(extractedKeywords, 1),
                getKeywordOrNull(extractedKeywords, 2),
                getKeywordOrNull(extractedKeywords, 3),
                getKeywordOrNull(extractedKeywords, 4),
                pageable
        );

        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 유사 정책 조회 (같은 카테고리 기반)
     */
    public List<PolicyListResponse> getSimilarPolicies(Long policyId, Long userId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);

        List<PolicyEntity> similarPolicies = policyRepository.findSimilarPoliciesByCategory(policyId, pageable);
        fetchCollections(similarPolicies);

        return similarPolicies.stream()
                .map(policy -> policyMapper.toListResponse(policy, false))
                .toList();
    }

    private PageResponse<PolicyListResponse> convertToPageResponse(Page<PolicyEntity> policyPage, Long userId) {
        List<PolicyEntity> policies = policyPage.getContent();
        fetchCollections(policies);

        Map<Long, Boolean> bookmarkMap = getBookmarkMap(policies, userId);

        Page<PolicyListResponse> mappedPage = policyPage.map(policy ->
                policyMapper.toListResponse(policy, bookmarkMap.getOrDefault(policy.getId(), false))
        );

        return new PageResponse<>(mappedPage);
    }

    /**
     * 북마크 여부 Map 생성
     * - userId가 null이면 빈 Map 반환 (모든 북마크 false)
     * - userId가 있으면 해당 사용자의 북마크 여부를 Map으로 반환
     */
    private Map<Long, Boolean> getBookmarkMap(List<PolicyEntity> policies, Long userId) {
        if (userId == null || policies.isEmpty()) {
            return Map.of();
        }

        UserEntity user = userFindService.findUser(userId);
        if (user == null) {
            return Map.of();
        }

        List<PolicyBookmarkEntity> bookmarks = policyBookmarkRepository.findByUser(user);

        Set<Long> bookmarkedPolicyIds = bookmarks.stream()
                .map(bookmark -> bookmark.getPolicy().getId())
                .collect(Collectors.toSet());

        return policies.stream()
                .collect(Collectors.toMap(
                        PolicyEntity::getId,
                        policy -> bookmarkedPolicyIds.contains(policy.getId())
                ));
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
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("형태소 분석 실패: {}", text, e);
            return List.of(text);
        }
    }

    /**
     * 리스트에서 인덱스의 값을 가져오거나 null 반환
     */
    private String getKeywordOrNull(List<String> keywords, int index) {
        return index < keywords.size() ? keywords.get(index) : null;
    }

    private void fetchCollections(List<PolicyEntity> policies) {
        if (policies == null || policies.isEmpty()) return;

        policyRepository.findWithCategories(policies);
        policyRepository.findWithKeywords(policies);
        policyRepository.findWithRegions(policies);
    }


    private void fetchCollectionsForDetail(PolicyEntity policy) {
        List<PolicyEntity> singleList = List.of(policy);
        policyRepository.findWithCategories(singleList);
        policyRepository.findWithKeywords(singleList);
        policyRepository.findWithRegions(singleList);
    }
}