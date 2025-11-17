package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserBadgeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeCommandService {
    private final UserBadgeRepository userBadgeRepository;
    private final UserFindService userFindService;
    private final BadgeFindService badgeFindService;

    public void grantBadge(Long userId, Long badgeId) {
        try {
            UserEntity user = userFindService.findUser(userId);
            BadgeEntity badge = badgeFindService.findBadge(badgeId);

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
            return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                    .orElseGet(() -> createUserBadge(userId, badgeId));
        } catch (DataIntegrityViolationException e) {
            log.warn("동시성 이슈 감지 - 재조회 시도. userId: {}, badgeId: {}", userId, badgeId);
            return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                    .orElseThrow(() -> new IllegalStateException("뱃지 부여 중 오류가 발생했습니다.", e));
        }
    }

    private UserBadgeEntity createUserBadge(Long userId, Long badgeId) {
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