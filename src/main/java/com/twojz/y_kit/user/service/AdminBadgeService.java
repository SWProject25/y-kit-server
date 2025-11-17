package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBadgeService {
    private final BadgeRepository badgeRepository;
    private final BadgeFindService badgeFindService;

    public BadgeEntity createBadge(String name, String description, String iconUrl) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("뱃지 이름은 필수입니다.");
        }

        BadgeEntity badge = BadgeEntity.builder()
                .name(name)
                .description(description)
                .iconUrl(iconUrl)
                .build();

        return badgeRepository.save(badge);
    }

    public BadgeEntity updateBadge(Long badgeId, String name, String description, String iconUrl) {
        BadgeEntity badge = badgeFindService.findBadge(badgeId);
        badge.update(name, description, iconUrl);
        return badge;
    }

    public void deleteBadge(Long badgeId) {
        BadgeEntity badge = badgeFindService.findBadge(badgeId);
        badgeRepository.delete(badge);
    }

    @Transactional(readOnly = true)
    public List<BadgeEntity> getAllBadges() {
        return badgeRepository.findAll();
    }
}