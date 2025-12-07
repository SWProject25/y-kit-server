package com.twojz.y_kit.user.service;

import com.twojz.y_kit.community.repository.CommunityBookmarkRepository;
import com.twojz.y_kit.community.repository.CommunityCommentRepository;
import com.twojz.y_kit.community.repository.CommunityLikeRepository;
import com.twojz.y_kit.community.repository.CommunityRepository;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.repository.*;
import com.twojz.y_kit.hotdeal.repository.HotDealBookmarkRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealLikeRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.policy.repository.PolicyNotificationRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import com.twojz.y_kit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeleteService {
    private final UserRepository userRepository;
    private final UserFindService userFindService;

    // User related repositories
    private final UserDeviceRepository userDeviceRepository;
    private final UserBadgeRepository userBadgeRepository;

    // Community related repositories
    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityBookmarkRepository communityBookmarkRepository;

    // HotDeal related repositories
    private final HotDealRepository hotDealRepository;
    private final HotDealCommentRepository hotDealCommentRepository;
    private final HotDealLikeRepository hotDealLikeRepository;
    private final HotDealBookmarkRepository hotDealBookmarkRepository;

    // Group related repositories
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final GroupPurchaseCommentRepository groupPurchaseCommentRepository;
    private final GroupPurchaseBookmarkRepository groupPurchaseBookmarkRepository;
    private final GroupPurchaseLikeRepository groupPurchaseLikeRepository;
    private final GroupPurchaseParticipantRepository groupPurchaseParticipantRepository;

    // Policy related repositories
    private final PolicyBookmarkRepository policyBookmarkRepository;
    private final PolicyNotificationRepository policyNotificationRepository;

    // Notification repository
    private final NotificationRepository notificationRepository;

    /**
     * 사용자 탈퇴 처리
     * 사용자와 관련된 모든 데이터를 삭제합니다.
     *
     * @param userId 탈퇴할 사용자 ID
     */
    @Transactional
    public void withdrawUser(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        // 1. 알림 삭제
        deleteNotifications(user);

        // 2. 정책 관련 데이터 삭제
        deletePolicyRelatedData(user);

        // 3. 커뮤니티 관련 데이터 삭제
        deleteCommunityRelatedData(user);

        // 4. 핫딜 관련 데이터 삭제
        deleteHotDealRelatedData(user);

        // 5. 공동구매 관련 데이터 삭제
        deleteGroupBuyingRelatedData(user);

        // 6. 사용자 뱃지 삭제
        deleteUserBadges(user);

        // 7. 사용자 디바이스 및 리프레시 토큰 삭제
        deleteUserDevices(user);

        // 8. 사용자 엔티티 삭제
        userRepository.delete(user);

        log.info("사용자 탈퇴 완료 - userId: {}", userId);
    }

    private void deleteNotifications(UserEntity user) {
        notificationRepository.deleteByUser(user);
    }

    private void deletePolicyRelatedData(UserEntity user) {
        policyBookmarkRepository.deleteByUser(user);
        policyNotificationRepository.deleteByUser(user);
    }

    private void deleteCommunityRelatedData(UserEntity user) {
        // 사용자가 작성한 커뮤니티 게시글 조회
        var userCommunities = communityRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // 각 게시글에 달린 모든 북마크, 좋아요, 댓글 삭제 (다른 사용자가 단 것도 포함)
        for (var community : userCommunities) {
            communityBookmarkRepository.deleteByCommunity(community);
            communityLikeRepository.deleteByCommunity(community);
            communityCommentRepository.deleteByCommunity(community);
        }

        // 사용자가 다른 게시글에 단 북마크, 좋아요, 댓글 삭제
        communityBookmarkRepository.deleteByUser(user);
        communityLikeRepository.deleteByUser(user);
        communityCommentRepository.deleteByUser(user);

        // 사용자가 작성한 게시글 삭제
        communityRepository.deleteByUser(user);
    }

    private void deleteHotDealRelatedData(UserEntity user) {
        // 사용자가 작성한 핫딜 게시글 조회
        var userHotDeals = hotDealRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // 각 게시글에 달린 모든 북마크, 좋아요, 댓글 삭제 (다른 사용자가 단 것도 포함)
        for (var hotDeal : userHotDeals) {
            hotDealBookmarkRepository.deleteByHotDeal(hotDeal);
            hotDealLikeRepository.deleteByHotDeal(hotDeal);
            hotDealCommentRepository.deleteByHotDeal(hotDeal);
        }

        // 사용자가 다른 게시글에 단 북마크, 좋아요, 댓글 삭제
        hotDealBookmarkRepository.deleteByUser(user);
        hotDealLikeRepository.deleteByUser(user);
        hotDealCommentRepository.deleteByUser(user);

        // 사용자가 작성한 게시글 삭제
        hotDealRepository.deleteByUser(user);
    }

    private void deleteGroupBuyingRelatedData(UserEntity user) {
        // 사용자가 작성한 공동구매 게시글 조회
        List<GroupPurchaseEntity> userGroupPurchases = groupPurchaseRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // 각 공동구매 게시글에 달린 모든 북마크, 좋아요, 댓글, 참가자 정보 삭제 (다른 사용자가 단 것도 포함)
        for (GroupPurchaseEntity groupPurchase : userGroupPurchases) {
            groupPurchaseBookmarkRepository.deleteByGroupPurchase(groupPurchase);
            groupPurchaseLikeRepository.deleteByGroupPurchase(groupPurchase);
            groupPurchaseCommentRepository.deleteByGroupPurchase(groupPurchase);
            groupPurchaseParticipantRepository.deleteByGroupPurchase(groupPurchase);
        }
        log.info("공동구매 게시글 관련 데이터 삭제 완료 - userId: {}, 게시글 수: {}", user.getId(), userGroupPurchases.size());

        // 사용자가 다른 게시글에 단 북마크, 좋아요, 댓글, 참가한 정보 삭제
        groupPurchaseBookmarkRepository.deleteByUser(user);
        groupPurchaseLikeRepository.deleteByUser(user);
        groupPurchaseCommentRepository.deleteByUser(user);
        groupPurchaseParticipantRepository.deleteByUser(user);

        // 사용자가 작성한 공동구매 게시글 삭제
        groupPurchaseRepository.deleteByUser(user);

        log.info("공동구매 관련 데이터 삭제 완료 - userId: {}", user.getId());
    }

    private void deleteUserBadges(UserEntity user) {
        userBadgeRepository.deleteByUserId(user.getId());
        log.info("사용자 뱃지 삭제 완료 - userId: {}", user.getId());
    }

    private void deleteUserDevices(UserEntity user) {
        userDeviceRepository.deleteByUser(user);
        log.info("사용자 디바이스 삭제 완료 - userId: {}", user.getId());
    }
}
