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
import com.twojz.y_kit.policy.service.persistence.PolicyApplicationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyBookmarkPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyCategoryPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDetailPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyEntityPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyKeywordPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyNotificationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyQualificationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyRegionPersistenceService;
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
    private final PolicyEntityPersistenceService policyEntityPersistenceService;
    private final PolicyDetailPersistenceService policyDetailPersistenceService;
    private final PolicyApplicationPersistenceService policyApplicationPersistenceService;
    private final PolicyQualificationPersistenceService policyQualificationPersistenceService;
    private final PolicyCategoryPersistenceService policyCategoryPersistenceService;
    private final PolicyKeywordPersistenceService policyKeywordPersistenceService;
    private final PolicyRegionPersistenceService policyRegionPersistenceService;
    private final PolicyBookmarkPersistenceService policyBookmarkPersistenceService;
    private final PolicyNotificationPersistenceService policyNotificationPersistenceService;
    private final UserFindService userFindService;

    /**
     * 정책 북마크 토글
     */
    public void toggleBookmark(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        boolean exists = policyBookmarkPersistenceService.existsByPolicyAndUser(policy, user);

        if (exists) {
            policyBookmarkPersistenceService.findByPolicyAndUser(policy, user)
                    .ifPresent(bookmark -> {
                        policyBookmarkPersistenceService.delete(bookmark);
                        policy.decreaseBookmarkCount();
                    });
        } else {
            PolicyBookmarkEntity bookmark = PolicyBookmarkEntity.builder()
                    .policy(policy)
                    .user(user)
                    .build();
            policyBookmarkPersistenceService.save(bookmark);
            policy.increaseBookmarkCount();
        }
    }

    /**
     * 정책 알림 신청 토글
     */
    public void toggleNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        policyNotificationPersistenceService.findByPolicyAndUser(policy, user)
                .ifPresentOrElse(
                        notification -> {
                            policyNotificationPersistenceService.delete(notification);
                            log.info("정책 알림 신청 취소 - policyId: {}, userId: {}", policyId, userId);
                        },
                        () -> {
                            PolicyNotificationEntity notification = PolicyNotificationEntity.builder()
                                    .policy(policy)
                                    .user(user)
                                    .build();
                            policyNotificationPersistenceService.save(notification);
                            log.info("정책 알림 신청 완료 - policyId: {}, userId: {}", policyId, userId);
                        }
                );
    }

    /**
     * 정책 북마크 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);
        return policyBookmarkPersistenceService.existsByPolicyAndUser(policy, user);
    }

    /**
     * 정책 알림 신청 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);
        return policyNotificationPersistenceService.existsByPolicyAndUser(policy, user);
    }

    /**
     * 내가 신청한 정책 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyNotificationResponse> getMyNotifications(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        return policyNotificationPersistenceService.findMyNotificationResponses(user);
    }

    /**
     * 정책 알림 신청 삭제 (by policyId)
     */
    public void cancelNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyEntityPersistenceService.findById(policyId);
        UserEntity user = userFindService.findUser(userId);

        policyNotificationPersistenceService.deleteByPolicyAndUser(policy, user);
        log.info("정책 알림 신청 삭제 완료 - policyId: {}, userId: {}", policyId, userId);
    }

    /**
     * 내가 북마크한 정책 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        List<PolicyBookmarkEntity> bookmarks = policyBookmarkPersistenceService.findByUserWithDetailOrderByCreatedAtDesc(user);
        List<PolicyEntity> policies = bookmarks.stream()
                .map(PolicyBookmarkEntity::getPolicy)
                .toList();

        Map<Long, PolicyDetailEntity> detailMap = policyDetailPersistenceService.findMapByPolicies(policies);
        Map<Long, PolicyApplicationEntity> applicationMap = policyApplicationPersistenceService.findMapByPolicies(policies);
        Map<Long, PolicyQualificationEntity> qualificationMap = policyQualificationPersistenceService.findMapByPolicies(policies);
        Map<Long, List<PolicyCategoryMapping>> categoryMap = policyCategoryPersistenceService.findMappingMapByPolicies(policies);
        Map<Long, List<PolicyKeywordMapping>> keywordMap = policyKeywordPersistenceService.findMappingMapByPolicies(policies);
        Map<Long, List<PolicyRegion>> regionMap = policyRegionPersistenceService.findMapByPolicies(policies);

        return bookmarks.stream()
                .map(PolicyBookmarkEntity::getPolicy)
                .map(policy -> PolicyListResponse.from(
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
