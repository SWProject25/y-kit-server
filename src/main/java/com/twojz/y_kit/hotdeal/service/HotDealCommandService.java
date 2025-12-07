package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.hotdeal.domain.entity.HotDealBookmarkEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealLikeEntity;
import com.twojz.y_kit.hotdeal.dto.request.HotDealCommentCreateRequest;
import com.twojz.y_kit.hotdeal.dto.request.HotDealCreateRequest;
import com.twojz.y_kit.hotdeal.dto.request.HotDealUpdateRequest;
import com.twojz.y_kit.hotdeal.repository.HotDealBookmarkRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealLikeRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.service.RegionFindService;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.BadgeCommandService;
import com.twojz.y_kit.user.service.BadgeFindService;
import com.twojz.y_kit.user.service.UserFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HotDealCommandService {
    private final HotDealRepository hotDealRepository;
    private final HotDealLikeRepository hotDealLikeRepository;
    private final HotDealBookmarkRepository hotDealBookmarkRepository;
    private final HotDealCommentRepository hotDealCommentRepository;
    private final UserFindService userFindService;
    private final RegionFindService regionFindService;
    private final HotDealFindService hotDealFindService;
    private final BadgeCommandService badgeCommandService;
    private final BadgeFindService badgeFindService;

    /**
     * 핫딜 생성
     */
    public Long createHotDeal(Long userId, HotDealCreateRequest request) {
        UserEntity user = userFindService.findUser(userId);

        // 첫 게시물인지 확인
        long userPostCount = hotDealRepository.countByUser(user);
        boolean isFirstPost = (userPostCount == 0);

        // 지역 정보 결정: regionCode가 있으면 우선 사용, 없으면 주소 기반 검색
        Region region = regionFindService.findRegionByAddress(
                request.getSido(),
                request.getSigungu(),
                request.getDong()
        );

        HotDealEntity hotDeal = HotDealEntity.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .placeName(request.getPlaceName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .url(request.getUrl())
                .dealType(request.getDealType())
                .category(request.getCategory())
                .region(region)
                .expiresAt(request.getExpiresAt())
                .build();

        Long hotDealId = hotDealRepository.save(hotDeal).getId();

        // 첫 게시물이면 뱃지 부여
        if (isFirstPost) {
            try {
                BadgeEntity badge = badgeFindService.findByName("핫딜 첫 공유");
                badgeCommandService.grantBadgeIfNotExists(userId, badge.getId());
                log.info("🏅 '핫딜 첫 공유' 뱃지 부여 완료 - userId: {}", userId);
            } catch (Exception e) {
                log.warn("뱃지 부여 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        }

        return hotDealId;
    }

    /**
     * 핫딜 수정
     */
    public void updateHotDeal(Long hotDealId, Long userId, HotDealUpdateRequest request) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);

        validateOwnership(hotDeal, userId, "수정");

        Region region = regionFindService.findRegionCode(request.getRegionCode());

        hotDeal.update(
                request.getTitle(),
                request.getContent(),
                request.getPlaceName(),
                request.getExpiresAt(),
                request.getDealType(),
                request.getCategory(),
                region,
                request.getUrl(),
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress()
        );
    }

    /**
     * 핫딜 삭제
     */
    public void deleteHotDeal(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);

        validateOwnership(hotDeal, userId, "삭제");

        hotDealRepository.delete(hotDeal);
    }

    /**
     * 좋아요 토글
     */
    public void toggleLike(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        UserEntity user = userFindService.findUser(userId);

        hotDealLikeRepository.findByHotDealAndUser(hotDeal, user)
                .ifPresentOrElse(
                        like -> {
                            hotDealLikeRepository.delete(like);
                            hotDeal.decreaseLikeCount();
                        },
                        () -> {
                            HotDealLikeEntity newLike = HotDealLikeEntity.builder()
                                    .hotDeal(hotDeal)
                                    .user(user)
                                    .build();
                            hotDealLikeRepository.save(newLike);
                            hotDeal.increaseLikeCount();
                        }
                );
    }

    /**
     * 북마크 토글
     */
    public void toggleBookmark(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        UserEntity user = userFindService.findUser(userId);

        hotDealBookmarkRepository.findByHotDealAndUser(hotDeal, user)
                .ifPresentOrElse(
                        bookmark -> {
                            hotDealBookmarkRepository.delete(bookmark);
                            hotDeal.decreaseBookmarkCount();
                        },
                        () -> {
                            HotDealBookmarkEntity newBookmark = HotDealBookmarkEntity.builder()
                                    .hotDeal(hotDeal)
                                    .user(user)
                                    .build();
                            hotDealBookmarkRepository.save(newBookmark);
                            hotDeal.increaseBookmarkCount();
                        }
                );
    }

    /**
     * 댓글 생성
     */
    public Long createComment(Long hotDealId, Long userId, HotDealCommentCreateRequest request) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        UserEntity user = userFindService.findUser(userId);

        HotDealCommentEntity comment = HotDealCommentEntity.builder()
                .hotDeal(hotDeal)
                .user(user)
                .content(request.getContent())
                .build();

        HotDealCommentEntity saved = hotDealCommentRepository.save(comment);
        hotDeal.increaseCommentCount();

        return saved.getId();
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId, Long userId) {
        HotDealCommentEntity comment = hotDealCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        HotDealEntity hotDeal = comment.getHotDeal();

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }
        hotDeal.decreaseCommentCount();
        hotDealCommentRepository.delete(comment);
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount(Long hotDealId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        hotDeal.increaseViewCount();
        log.debug("👁️ 조회수 증가 - hotDealId: {}, viewCount: {}", hotDealId, hotDeal.getViewCount());
    }

    /**
     * 소유권 검증 (공통 메서드)
     */
    private void validateOwnership(HotDealEntity hotDeal, Long userId, String action) {
        if (!hotDeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(action + " 권한이 없습니다.");
        }
    }
}