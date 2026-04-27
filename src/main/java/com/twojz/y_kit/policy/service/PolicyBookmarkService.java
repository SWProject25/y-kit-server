package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyCategoryMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyKeywordMapping;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyRegion;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.dto.response.PolicyNotificationResponse;
import java.util.Map;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyBookmarkService {
    private final PolicyEntityFindService policyEntityFindService;
    private final PolicyDetailFindService policyDetailFindService;
    private final PolicyApplicationFindService policyApplicationFindService;
    private final PolicyQualificationFindService policyQualificationFindService;
    private final PolicyCategoryMappingFindService policyCategoryMappingFindService;
    private final PolicyKeywordMappingFindService policyKeywordMappingFindService;
    private final PolicyRegionFindService policyRegionFindService;
    private final PolicyBookmarkFindService policyBookmarkFindService;
    private final PolicyBookmarkCommandService policyBookmarkCommandService;
    private final PolicyNotificationFindService policyNotificationFindService;
    private final PolicyNotificationCommandService policyNotificationCommandService;
    private final UserFindService userFindService;
    private final PolicyMapper policyMapper;

    /**
     * 정책 북마크 토글
     */
    public void toggleBookmark(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityFindService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        boolean exists = policyBookmarkFindService.existsByPolicyAndUser(policy, user);

        if (exists) {
            policyBookmarkFindService.findByPolicyAndUser(policy, user)
                    .ifPresent(bookmark -> {
                        policyBookmarkCommandService.delete(bookmark);
                        policy.decreaseBookmarkCount();
                    });
        } else {
            PolicyBookmarkEntity bookmark = PolicyBookmarkEntity.builder()
                    .policy(policy)
                    .user(user)
                    .build();
            policyBookmarkCommandService.save(bookmark);
            policy.increaseBookmarkCount();
        }
    }

    /**
     * 정책 알림 신청 토글
     */
    public void toggleNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityFindService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        policyNotificationFindService.findByPolicyAndUser(policy, user)
                .ifPresentOrElse(
                        notification -> {
                            policyNotificationCommandService.delete(notification);
                            log.info("정책 알림 신청 취소 - policyId: {}, userId: {}", policyId, userId);
                        },
                        () -> {
                            PolicyNotificationEntity notification = PolicyNotificationEntity.builder()
                                    .policy(policy)
                                    .user(user)
                                    .build();
                            policyNotificationCommandService.save(notification);
                            log.info("정책 알림 신청 완료 - policyId: {}, userId: {}", policyId, userId);
                        }
                );
    }

    /**
     * 정책 북마크 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityFindService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);
        return policyBookmarkFindService.existsByPolicyAndUser(policy, user);
    }

    /**
     * 정책 알림 신청 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityFindService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);
        return policyNotificationFindService.existsByPolicyAndUser(policy, user);
    }

    /**
     * 내가 신청한 정책 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyNotificationResponse> getMyNotifications(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        return policyNotificationFindService.findMyNotificationResponses(user);
    }

    /**
     * 정책 알림 신청 삭제 (by policyId)
     */
    public void cancelNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityFindService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        policyNotificationCommandService.deleteByPolicyAndUser(policy, user);
        log.info("정책 알림 신청 삭제 완료 - policyId: {}, userId: {}", policyId, userId);
    }

    /**
     * 내가 북마크한 정책 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        List<PolicyBookmarkEntity> bookmarks = policyBookmarkFindService.findByUserOrderByCreatedAtDesc(user);
        List<PolicyEntity> policies = bookmarks.stream()
                .map(PolicyBookmarkEntity::getPolicy)
                .toList();

        Map<Long, PolicyDetailEntity> detailMap = policyDetailFindService.findMapByPolicies(policies);
        Map<Long, PolicyApplicationEntity> applicationMap = policyApplicationFindService.findMapByPolicies(policies);
        Map<Long, PolicyQualificationEntity> qualificationMap = policyQualificationFindService.findMapByPolicies(policies);
        Map<Long, List<PolicyCategoryMapping>> categoryMap = policyCategoryMappingFindService.findMapByPolicies(policies);
        Map<Long, List<PolicyKeywordMapping>> keywordMap = policyKeywordMappingFindService.findMapByPolicies(policies);
        Map<Long, List<PolicyRegion>> regionMap = policyRegionFindService.findMapByPolicies(policies);

        return bookmarks.stream()
                .map(PolicyBookmarkEntity::getPolicy)
                .map(policy -> policyMapper.toListResponse(
                        policy,
                        detailMap.get(policy.getId()),
                        applicationMap.get(policy.getId()),
                        qualificationMap.get(policy.getId()),
                        categoryMap.getOrDefault(policy.getId(), List.of()),
                        keywordMap.getOrDefault(policy.getId(), List.of()),
                        regionMap.getOrDefault(policy.getId(), List.of()),
                        true
                ))
                .toList();
    }
}
