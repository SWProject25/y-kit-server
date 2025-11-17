package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserBadgeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeCommandService {
    private final UserBadgeRepository userBadgeRepository;
    private final UserFindService userFindService;
    private final BadgeFindService badgeFindService;

    public UserBadgeEntity grantBadge(Long userId, Long badgeId) {
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

        return userBadgeRepository.save(userBadge);
    }

    public UserBadgeEntity grantBadgeIfNotExists(Long userId, Long badgeId) {
        return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseGet(() -> {
                    UserEntity user = userFindService.findUser(userId);
                    BadgeEntity badge = badgeFindService.findBadge(badgeId);
                    return userBadgeRepository.save(UserBadgeEntity.builder()
                            .user(user)
                            .badge(badge)
                            .build());
                });
    }

    public void revokeBadge(Long userId, Long badgeId) {
        // 존재 여부 확인이 필요한 경우
        userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 뱃지입니다."));

        userBadgeRepository.deleteByUserIdAndBadgeId(userId, badgeId);
    }

    public void revokeBadgeIfExists(Long userId, Long badgeId) {
        userBadgeRepository.deleteByUserIdAndBadgeId(userId, badgeId);
    }
}