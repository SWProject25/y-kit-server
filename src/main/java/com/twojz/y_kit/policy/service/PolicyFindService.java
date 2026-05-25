package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.dto.response.PolicyCategoryResponse;
import com.twojz.y_kit.policy.dto.response.PolicyDetailResponse;
import com.twojz.y_kit.policy.dto.response.PolicyKeywordResponse;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.service.persistence.PolicyApplicationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyBookmarkPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyCategoryPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDetailPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDocumentPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyEntityPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyKeywordPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyQualificationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyRegionPersistenceService;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyFindService {

    private final PolicyEntityPersistenceService policyEntityPersistenceService;
    private final PolicyDetailPersistenceService policyDetailPersistenceService;
    private final PolicyApplicationPersistenceService policyApplicationPersistenceService;
    private final PolicyQualificationPersistenceService policyQualificationPersistenceService;
    private final PolicyDocumentPersistenceService policyDocumentPersistenceService;
    private final PolicyCategoryPersistenceService policyCategoryPersistenceService;
    private final PolicyKeywordPersistenceService policyKeywordPersistenceService;
    private final PolicyRegionPersistenceService policyRegionPersistenceService;
    private final PolicyBookmarkPersistenceService policyBookmarkPersistenceService;
    private final UserFindService userFindService;

    public List<PolicyEntity> getPoliciesByIds(List<Long> ids) {
        return policyEntityPersistenceService.findByIds(ids);
    }

    public PolicyEntity getPolicyById(Long id) {
        return policyEntityPersistenceService.findById(id);
    }

    public PageResponse<PolicyListResponse> getPolicyList(Long userId, Pageable pageable) {
        Page<PolicyEntity> policyPage = policyEntityPersistenceService.findAllActive(pageable);
        return convertToPageResponse(policyPage, userId);
    }

    @Transactional
    public PolicyDetailResponse getPolicyDetail(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);

        PolicyDetailEntity detail = policyDetailPersistenceService.findByPolicyOrThrow(policy);
        PolicyApplicationEntity application = policyApplicationPersistenceService.findNullableByPolicy(policy);
        PolicyQualificationEntity qualification = policyQualificationPersistenceService.findNullableByPolicy(policy);
        PolicyDocumentEntity document = policyDocumentPersistenceService.findNullableByPolicy(policy);
        List<PolicyCategoryMapping> categoryMappings = policyCategoryPersistenceService.findMappingsByPolicy(policy);
        List<PolicyKeywordMapping> keywordMappings = policyKeywordPersistenceService.findMappingsByPolicy(policy);
        List<PolicyRegion> regions = policyRegionPersistenceService.findByPolicy(policy);

        policy.increaseViewCount();

        boolean isBookmarked = false;
        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isBookmarked = policyBookmarkPersistenceService.existsByPolicyAndUser(policy, user);
        }

        return PolicyDetailResponse.from(
                policy, detail, application, qualification, document,
                categoryMappings, keywordMappings, regions, isBookmarked);
    }

    /**
     * 사용자 정보 기반 추천 정책 조회 (전체 프로필 활용)
     */
    public PageResponse<PolicyListResponse> getRecommendedPoliciesByUser(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);

        if (user.getProfileStatus() != ProfileStatus.COMPLETED) {
            throw new IllegalStateException("프로필 정보가 완료되지 않았습니다.");
        }

        Page<PolicyEntity> policyPage = policyEntityPersistenceService.findRecommendedWithProfile(
                user.calculateAge(),
                user.getRegion().getCode(),
                user.getEmploymentStatus(),
                user.getEducationLevel(),
                user.getMajor(),
                pageable
        );

        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 인기 정책 조회
     */
    public PageResponse<PolicyListResponse> getPopularPolicies(String sortBy, Long userId, Pageable pageable) {
        Page<PolicyEntity> policyPage;

        if ("bookmarkCount".equals(sortBy)) {
            policyPage = policyEntityPersistenceService.findPopularByBookmarkCount(pageable);
        } else {
            policyPage = policyEntityPersistenceService.findPopularByViewCount(pageable);
        }

        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 마감 임박 정책 조회
     */
    public PageResponse<PolicyListResponse> getDeadlineSoonPolicies(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<PolicyEntity> policyPage = policyEntityPersistenceService.findDeadlineSoon(today, pageable);
        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 모든 정책 카테고리 조회
     */
    public List<PolicyCategoryResponse> getAllCategories() {
        return policyCategoryPersistenceService.findAllActive()
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
        return policyKeywordPersistenceService.findTop50ByUsageCount()
                .stream()
                .map(keyword -> PolicyKeywordResponse.builder()
                        .id(keyword.getId())
                        .keyword(keyword.getKeyword())
                        .usageCount(keyword.getUsageCount())
                        .build())
                .toList();
    }

    /**
     * 정책 검색 및 필터링 (QueryDSL 기반)
     * - categoryIds: PolicyCategoryEntity의 ID 리스트
     * - keywordIds: PolicyKeywordEntity의 ID 리스트
     * - keyword: 정책명/설명 텍스트 검색 (형태소 분석 후 OR 연산)
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

        Page<PolicyEntity> policyPage = policyEntityPersistenceService.searchPolicies(
                categoryIds, keywordIds, extractedKeywords, pageable);

        return convertToPageResponse(policyPage, userId);
    }

    /**
     * 유사 정책 조회 (같은 카테고리 기반)
     */
    public List<PolicyListResponse> getSimilarPolicies(Long policyId, Long userId, int limit) {
        List<PolicyEntity> similarPolicies = policyEntityPersistenceService.findSimilarByCategory(policyId, limit);

        if (similarPolicies.isEmpty()) {
            return List.of();
        }

        RelatedPolicyMaps maps = fetchRelatedMaps(similarPolicies);
        Map<Long, Boolean> bookmarkMap = getBookmarkMap(similarPolicies, userId);

        return similarPolicies.stream()
                .map(policy -> {
                    Long id = policy.getId();
                    return PolicyListResponse.from(
                            policy,
                            maps.details().get(id),
                            maps.applications().get(id),
                            maps.qualifications().get(id),
                            maps.categories().getOrDefault(id, List.of()),
                            maps.keywords().getOrDefault(id, List.of()),
                            maps.regions().getOrDefault(id, List.of()),
                            bookmarkMap.getOrDefault(id, false));
                })
                .toList();
    }

    private PageResponse<PolicyListResponse> convertToPageResponse(Page<PolicyEntity> policyPage, Long userId) {
        List<PolicyEntity> policies = policyPage.getContent();

        if (policies.isEmpty()) {
            return new PageResponse<>(policyPage.map(policy -> (PolicyListResponse) null));
        }

        RelatedPolicyMaps maps = fetchRelatedMaps(policies);
        Map<Long, Boolean> bookmarkMap = getBookmarkMap(policies, userId);

        Page<PolicyListResponse> mappedPage = policyPage.map(policy -> {
            Long id = policy.getId();
            return PolicyListResponse.from(
                    policy,
                    maps.details().get(id),
                    maps.applications().get(id),
                    maps.qualifications().get(id),
                    maps.categories().getOrDefault(id, List.of()),
                    maps.keywords().getOrDefault(id, List.of()),
                    maps.regions().getOrDefault(id, List.of()),
                    bookmarkMap.getOrDefault(id, false));
        });

        return new PageResponse<>(mappedPage);
    }

    /**
     * 정책 목록에 대한 연관 엔티티를 일괄 조회 (N+1 방지)
     */
    private RelatedPolicyMaps fetchRelatedMaps(List<PolicyEntity> policies) {
        Map<Long, PolicyDetailEntity> detailMap = policyDetailPersistenceService.findMapByPolicies(policies);
        Map<Long, PolicyApplicationEntity> applicationMap = policyApplicationPersistenceService.findMapByPolicies(policies);
        Map<Long, PolicyQualificationEntity> qualificationMap = policyQualificationPersistenceService.findMapByPolicies(policies);
        Map<Long, List<PolicyCategoryMapping>> categoryMap = policyCategoryPersistenceService.findMappingMapByPolicies(policies);
        Map<Long, List<PolicyKeywordMapping>> keywordMap = policyKeywordPersistenceService.findMappingMapByPolicies(policies);
        Map<Long, List<PolicyRegion>> regionMap = policyRegionPersistenceService.findMapByPolicies(policies);

        return new RelatedPolicyMaps(detailMap, applicationMap, qualificationMap,
                categoryMap, keywordMap, regionMap);
    }

    /**
     * 북마크 여부 Map 생성 (N+1 문제 해결)
     */
    private Map<Long, Boolean> getBookmarkMap(List<PolicyEntity> policies, Long userId) {
        if (userId == null || policies.isEmpty()) {
            return Map.of();
        }

        UserEntity user = userFindService.findUser(userId);
        if (user == null) {
            return Map.of();
        }

        List<Long> policyIds = policies.stream()
                .map(PolicyEntity::getId)
                .toList();

        Set<Long> bookmarkedPolicyIds = new HashSet<>(policyBookmarkPersistenceService
                .findBookmarkedPolicyIdsByUserAndPolicyIds(user, policyIds));

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

    private record RelatedPolicyMaps(
            Map<Long, PolicyDetailEntity> details,
            Map<Long, PolicyApplicationEntity> applications,
            Map<Long, PolicyQualificationEntity> qualifications,
            Map<Long, List<PolicyCategoryMapping>> categories,
            Map<Long, List<PolicyKeywordMapping>> keywords,
            Map<Long, List<PolicyRegion>> regions
    ) {}
}
