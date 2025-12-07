package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 뱃지 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeFindService {
    private final BadgeRepository badgeRepository;

    public BadgeEntity findBadge(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뱃지입니다."));
    }

    public List<BadgeEntity> findAllBadges() {
        return badgeRepository.findAll();
    }

    public boolean existsBadge(Long badgeId) {
        return badgeRepository.existsById(badgeId);
    }

    public BadgeEntity findByName(String name) {
        return badgeRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뱃지입니다: " + name));
    }
}