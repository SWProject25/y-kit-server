package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.policy.domain.entity.PolicyBookmarkEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.policy.dto.response.PolicyListResponse;
import com.twojz.y_kit.policy.dto.response.PolicyNotificationResponse;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.policy.repository.PolicyNotificationRepository;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyBookmarkService {
    private final PolicyBookmarkRepository policyBookmarkRepository;
    private final PolicyNotificationRepository policyNotificationRepository;
    private final PolicyRepository policyRepository;
    private final UserFindService userFindService;
    private final PolicyMapper policyMapper;

    /**
     * 정책 북마크 토글
     */
    public void toggleBookmark(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        UserEntity user = userFindService.findUser(userId);

        boolean exists = policyBookmarkRepository.existsByPolicyAndUser(policy, user);

        if (exists) {
            // 북마크 삭제
            policyBookmarkRepository.findByPolicyAndUser(policy, user)
                    .ifPresent(bookmark -> {
                        policyBookmarkRepository.delete(bookmark);
                        policy.decreaseBookmarkCount();
                    });
        } else {
            // 북마크 추가
            PolicyBookmarkEntity bookmark = PolicyBookmarkEntity.builder()
                    .policy(policy)
                    .user(user)
                    .build();
            policyBookmarkRepository.save(bookmark);
            policy.increaseBookmarkCount();
        }
    }

    /**
     * 정책 알림 신청 토글
     */
    public void toggleNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        UserEntity user = userFindService.findUser(userId);

        policyNotificationRepository.findByPolicyAndUser(policy, user)
                .ifPresentOrElse(
                        notification -> {
                            // 알림 신청 취소
                            policyNotificationRepository.delete(notification);
                            log.info("정책 알림 신청 취소 - policyId: {}, userId: {}", policyId, userId);
                        },
                        () -> {
                            // 알림 신청
                            PolicyNotificationEntity notification = PolicyNotificationEntity.builder()
                                    .policy(policy)
                                    .user(user)
                                    .build();
                            policyNotificationRepository.save(notification);
                            log.info("정책 알림 신청 완료 - policyId: {}, userId: {}", policyId, userId);
                        }
                );
    }

    /**
     * 정책 북마크 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        UserEntity user = userFindService.findUser(userId);
        return policyBookmarkRepository.existsByPolicyAndUser(policy, user);
    }

    /**
     * 정책 알림 신청 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        UserEntity user = userFindService.findUser(userId);
        return policyNotificationRepository.existsByPolicyAndUser(policy, user);
    }

    /**
     * 내가 신청한 정책 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyNotificationResponse> getMyNotifications(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        List<PolicyNotificationEntity> notifications =
                policyNotificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(PolicyNotificationResponse::from)
                .toList();
    }

    /**
     * 정책 알림 신청 삭제 (by policyId)
     */
    public void cancelNotification(Long policyId, Long userId) {
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        UserEntity user = userFindService.findUser(userId);

        policyNotificationRepository.deleteByPolicyAndUser(policy, user);
        log.info("정책 알림 신청 삭제 완료 - policyId: {}, userId: {}", policyId, userId);
    }

    /**
     * 내가 북마크한 정책 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PolicyListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        // 북마크한 정책 엔티티 조회 (최신순)
        List<PolicyBookmarkEntity> bookmarks = policyBookmarkRepository
                .findByUserOrderByCreatedAtDesc(user);

        // PolicyListResponse로 변환 (모두 북마크된 상태이므로 isBookmarked = true)
        return bookmarks.stream()
                .map(bookmark -> policyMapper.toListResponse(bookmark.getPolicy(), true))
                .toList();
    }
}
