package com.twojz.y_kit.community.scheduler;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.repository.CommunityRepository;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.repository.GroupPurchaseRepository;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.service.BadgeCommandService;
import com.twojz.y_kit.user.service.BadgeFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingRankScheduler {
    private final CommunityRepository communityRepository;
    private final HotDealRepository hotDealRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final BadgeCommandService badgeCommandService;
    private final BadgeFindService badgeFindService;

    /**
     * 매 시간 정각에 실시간 순위를 갱신하고 1~3위에게 뱃지 부여
     * 크론 표현식: "0 0 * * * *" = 매 시간 0분 0초
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateTrendingRanksAndAwardBadges() {
        log.info("🔄 실시간 순위 갱신 및 뱃지 부여 시작...");

        try {
            // 뱃지 미리 조회
            BadgeEntity rank1Badge = badgeFindService.findByName("실시간 1위");
            BadgeEntity rank2Badge = badgeFindService.findByName("실시간 2위");
            BadgeEntity rank3Badge = badgeFindService.findByName("실시간 3위");

            // 커뮤니티 실시간 순위 처리
            processCommunityRankings(rank1Badge, rank2Badge, rank3Badge);

            // 동네핫딜 실시간 순위 처리
            processHotDealRankings(rank1Badge, rank2Badge, rank3Badge);

            // 공동구매 실시간 순위 처리
            processGroupPurchaseRankings(rank1Badge, rank2Badge, rank3Badge);

            log.info("✅ 실시간 순위 뱃지 부여 완료!");

        } catch (Exception e) {
            log.error("❌ 실시간 순위 갱신 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void processCommunityRankings(BadgeEntity rank1Badge, BadgeEntity rank2Badge, BadgeEntity rank3Badge) {
        try {
            List<CommunityEntity> topCommunities = communityRepository.findTrendingCommunities(
                    PageRequest.of(0, 3)
            );

            if (topCommunities.isEmpty()) {
                log.info("[커뮤니티] 실시간 순위에 게시물이 없습니다.");
                return;
            }

            awardRankingBadges(
                    topCommunities,
                    rank1Badge,
                    rank2Badge,
                    rank3Badge,
                    "커뮤니티",
                    entity -> entity.getUser().getId(),
                    CommunityEntity::getTitle
            );
        } catch (Exception e) {
            log.error("[커뮤니티] 실시간 순위 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private void processHotDealRankings(BadgeEntity rank1Badge, BadgeEntity rank2Badge, BadgeEntity rank3Badge) {
        try {
            List<HotDealEntity> topHotDeals = hotDealRepository.findTrendingHotDeals(
                    PageRequest.of(0, 3)
            );

            if (topHotDeals.isEmpty()) {
                log.info("[동네핫딜] 실시간 순위에 게시물이 없습니다.");
                return;
            }

            awardRankingBadges(
                    topHotDeals,
                    rank1Badge,
                    rank2Badge,
                    rank3Badge,
                    "동네핫딜",
                    entity -> entity.getUser().getId(),
                    HotDealEntity::getTitle
            );
        } catch (Exception e) {
            log.error("[동네핫딜] 실시간 순위 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private void processGroupPurchaseRankings(BadgeEntity rank1Badge, BadgeEntity rank2Badge, BadgeEntity rank3Badge) {
        try {
            List<GroupPurchaseEntity> topGroupPurchases = groupPurchaseRepository.findTrendingGroupPurchases(
                    PageRequest.of(0, 3)
            );

            if (topGroupPurchases.isEmpty()) {
                log.info("[공동구매] 실시간 순위에 게시물이 없습니다.");
                return;
            }

            awardRankingBadges(
                    topGroupPurchases,
                    rank1Badge,
                    rank2Badge,
                    rank3Badge,
                    "공동구매",
                    entity -> entity.getUser().getId(),
                    GroupPurchaseEntity::getTitle
            );
        } catch (Exception e) {
            log.error("[공동구매] 실시간 순위 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private <T> void awardRankingBadges(
            List<T> topItems,
            BadgeEntity rank1Badge,
            BadgeEntity rank2Badge,
            BadgeEntity rank3Badge,
            String category,
            java.util.function.Function<T, Long> userIdExtractor,
            java.util.function.Function<T, String> titleExtractor
    ) {
        // 1위 뱃지 부여
        if (topItems.size() >= 1) {
            T first = topItems.get(0);
            Long userId = userIdExtractor.apply(first);
            String title = titleExtractor.apply(first);
            try {
                badgeCommandService.grantBadgeIfNotExists(userId, rank1Badge.getId());
                log.info("🥇 [{}] 실시간 1위 뱃지 부여 - userId: {}, 게시물: '{}'", category, userId, title);
            } catch (Exception e) {
                log.warn("[{}] 1위 뱃지 부여 실패 - userId: {}, error: {}", category, userId, e.getMessage());
            }
        }

        // 2위 뱃지 부여
        if (topItems.size() >= 2) {
            T second = topItems.get(1);
            Long userId = userIdExtractor.apply(second);
            String title = titleExtractor.apply(second);
            try {
                badgeCommandService.grantBadgeIfNotExists(userId, rank2Badge.getId());
                log.info("🥈 [{}] 실시간 2위 뱃지 부여 - userId: {}, 게시물: '{}'", category, userId, title);
            } catch (Exception e) {
                log.warn("[{}] 2위 뱃지 부여 실패 - userId: {}, error: {}", category, userId, e.getMessage());
            }
        }

        // 3위 뱃지 부여
        if (topItems.size() >= 3) {
            T third = topItems.get(2);
            Long userId = userIdExtractor.apply(third);
            String title = titleExtractor.apply(third);
            try {
                badgeCommandService.grantBadgeIfNotExists(userId, rank3Badge.getId());
                log.info("🥉 [{}] 실시간 3위 뱃지 부여 - userId: {}, 게시물: '{}'", category, userId, title);
            } catch (Exception e) {
                log.warn("[{}] 3위 뱃지 부여 실패 - userId: {}, error: {}", category, userId, e.getMessage());
            }
        }
    }
}
