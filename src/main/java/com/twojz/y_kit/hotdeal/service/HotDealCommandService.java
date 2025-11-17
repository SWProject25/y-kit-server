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
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Long createHotDeal(Long userId, HotDealCreateRequest request) {
        UserEntity user = userFindService.findUser(userId);
        Region region = regionFindService.findRegion(request.getRegionCode());

        HotDealEntity hotDeal = HotDealEntity.builder()
                .user(user)
                .title(request.getTitle())
                .placeName(request.getPlaceName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .url(request.getUrl())
                .dealType(request.getDealType())
                .region(region)
                .expiresAt(request.getExpiresAt())
                .build();

        return hotDealRepository.save(hotDeal).getId();
    }

    public void updateHotDeal(Long hotDealId, Long userId, HotDealUpdateRequest request) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);

        if (!hotDeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Region region = regionFindService.findRegion(request.getRegionCode());

        hotDeal.update(
                request.getTitle(),
                request.getPlaceName(),
                request.getExpiresAt(),
                request.getDealType(),
                region,
                request.getUrl(),
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress()
        );
    }

    public void deleteHotDeal(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);

        if (!hotDeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        hotDealRepository.delete(hotDeal);
    }

    public void toggleLike(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);

        UserEntity user = userFindService.findUser(userId);

        HotDealLikeEntity like = hotDealLikeRepository.findByHotDealAndUser(hotDeal, user).orElse(null);

        if (like != null) {
            hotDealLikeRepository.delete(like);
        } else {
            hotDealLikeRepository.save(HotDealLikeEntity.builder()
                    .hotDeal(hotDeal)
                    .user(user)
                    .build());
        }
    }

    public void toggleBookmark(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        UserEntity user = userFindService.findUser(userId);

        HotDealBookmarkEntity bookmark = hotDealBookmarkRepository.findByHotDealAndUser(hotDeal, user).orElse(null);

        if (bookmark != null) {
            hotDealBookmarkRepository.delete(bookmark);
        } else {
            hotDealBookmarkRepository.save(HotDealBookmarkEntity.builder()
                    .hotDeal(hotDeal)
                    .user(user)
                    .build());
        }
    }

    public Long createComment(Long hotDealId, Long userId, HotDealCommentCreateRequest request) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        UserEntity user = userFindService.findUser(userId);

        HotDealCommentEntity comment = HotDealCommentEntity.builder()
                .hotDeal(hotDeal)
                .user(user)
                .content(request.getContent())
                .build();

        return hotDealCommentRepository.save(comment).getId();
    }

    public void deleteComment(Long commentId, Long userId) {
        HotDealCommentEntity comment = hotDealCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        hotDealCommentRepository.delete(comment);
    }

    public void increaseViewCount(Long hotDealId) {
        HotDealEntity hotDeal = hotDealFindService.findById(hotDealId);
        hotDeal.increaseViewCount();
    }
}