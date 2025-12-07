package com.twojz.y_kit.user.service;

import com.twojz.y_kit.notification.entity.NotificationType;
import com.twojz.y_kit.notification.service.NotificationService;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserBadgeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeCommandService {
    private final UserBadgeRepository userBadgeRepository;
    private final UserFindService userFindService;
    private final BadgeFindService badgeFindService;
    private final NotificationService notificationService;

    public void grantBadge(Long userId, Long badgeId) {
        try {
            UserEntity user = userFindService.findUser(userId);
            BadgeEntity badge = badgeFindService.findBadge(badgeId);

            userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                    .ifPresent(existing -> {
                        throw new IllegalStateException("이미 보유한 뱃지입니다.");
                    });

            UserBadgeEntity userBadge = UserBadgeEntity.builder()
                    .user(user)
                    .badge(badge)
                    .build();

            userBadgeRepository.save(userBadge);

        } catch (DataIntegrityViolationException e) {
            log.warn("동시성 이슈로 뱃지 부여 실패 - userId: {}, badgeId: {}", userId, badgeId);
            throw new IllegalStateException("이미 보유한 뱃지입니다.", e);
        }
    }

    public UserBadgeEntity grantBadgeIfNotExists(Long userId, Long badgeId) {
        try {
            UserBadgeEntity userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                    .orElseGet(() -> {
                        UserBadgeEntity newBadge = createUserBadge(userId, badgeId);
                        sendBadgeNotification(newBadge);
                        return newBadge;
                    });
            return userBadge;
        } catch (DataIntegrityViolationException e) {
            log.warn("동시성 이슈 감지 - 재조회 시도. userId: {}, badgeId: {}", userId, badgeId);
            return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                    .orElseThrow(() -> new IllegalStateException("뱃지 부여 중 오류가 발생했습니다.", e));
        }
    }

    private void sendBadgeNotification(UserBadgeEntity userBadge) {
        try {
            UserEntity user = userBadge.getUser();
            BadgeEntity badge = userBadge.getBadge();

            String title = "🏅 새로운 뱃지를 획득했습니다!";
            String body = String.format("'%s' 뱃지를 획득하셨습니다! 축하합니다!", badge.getName());
            String deepLink = "/mypage?tab=badges"; // 마이페이지 뱃지 탭으로 이동

            notificationService.sendNotification(user, title, body, NotificationType.BADGE, deepLink);
        } catch (Exception e) {
            log.error("뱃지 획득 알림 전송 실패", e);
        }
    }

    @Transactional
    public UserBadgeEntity createUserBadge(Long userId, Long badgeId) {
        UserEntity user = userFindService.findUser(userId);
        BadgeEntity badge = badgeFindService.findBadge(badgeId);

        UserBadgeEntity userBadge = UserBadgeEntity.builder()
                .user(user)
                .badge(badge)
                .build();

        return userBadgeRepository.save(userBadge);
    }

    public void revokeBadge(Long userId, Long badgeId) {
        UserBadgeEntity userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 뱃지입니다."));

        userBadgeRepository.delete(userBadge);
    }

    public void revokeBadgeIfExists(Long userId, Long badgeId) {
        userBadgeRepository.deleteByUserIdAndBadgeId(userId, badgeId);
    }
}