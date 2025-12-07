package com.twojz.y_kit.user.init;

import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BadgeInitializer implements ApplicationRunner {
    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createBadgeIfNotExists(
                "커뮤니티 첫 글",
                "커뮤니티에 첫 게시물을 작성했습니다!",
                "✍️"
        );

        createBadgeIfNotExists(
                "핫딜 첫 공유",
                "핫딜을 처음으로 공유했습니다!",
                "🔥"
        );

        createBadgeIfNotExists(
                "공동구매 첫 개설",
                "공동구매를 처음으로 개설했습니다!",
                "🛒"
        );

        createBadgeIfNotExists(
                "실시간 1위",
                "실시간 인기 순위 1위를 달성했습니다!",
                "🥇"
        );

        createBadgeIfNotExists(
                "실시간 2위",
                "실시간 인기 순위 2위를 달성했습니다!",
                "🥈"
        );

        createBadgeIfNotExists(
                "실시간 3위",
                "실시간 인기 순위 3위를 달성했습니다!",
                "🥉"
        );
    }

    private void createBadgeIfNotExists(String name, String description, String iconUrl) {
        badgeRepository.findByName(name).ifPresentOrElse(
                badge -> {
                    // 기존 뱃지 업데이트
                    badge.update(name, description, iconUrl);
                    badgeRepository.save(badge);
                },
                () -> {
                    // 새 뱃지 생성
                    BadgeEntity badge = BadgeEntity.builder()
                            .name(name)
                            .description(description)
                            .iconUrl(iconUrl)
                            .build();
                    badgeRepository.save(badge);
                }
        );
    }
}
