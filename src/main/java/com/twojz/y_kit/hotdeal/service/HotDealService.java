package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealBookmarkEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCommentEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealLikeEntity;
import com.twojz.y_kit.hotdeal.dto.request.HotDealCommentCreateRequest;
import com.twojz.y_kit.hotdeal.dto.request.HotDealCreateRequest;
import com.twojz.y_kit.hotdeal.dto.request.HotDealUpdateRequest;
import com.twojz.y_kit.hotdeal.dto.response.HotDealCommentResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealDetailResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.hotdeal.repository.HotDealBookmarkRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealLikeRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealSpecification;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.service.RegionService;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotDealService {
    private final HotDealRepository hotdealRepository;
    private final HotDealLikeRepository hotdealLikeRepository;
    private final HotDealBookmarkRepository hotdealBookmarkRepository;
    private final HotDealCommentRepository hotdealCommentRepository;
    private final UserService userService;
    private final RegionService regionService;

    @Transactional
    public Long createHotDeal(Long userId, HotDealCreateRequest request) {
        UserEntity user = userService.findUser(userId);
        Region region = regionService.findRegionName(request.getRegionCode());

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

        return hotdealRepository.save(hotDeal).getId();
    }

    @Transactional(readOnly = true)
    public HotDealDetailResponse getHotDealDetail(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));

        UserEntity user = userId != null ? userService.findUser(userId) : null;

        boolean isLiked = user != null && hotdealLikeRepository.existsByHotDealAndUser(hotDeal, user);
        boolean isBookmarked = user != null && hotdealBookmarkRepository.existsByHotDealAndUser(hotDeal, user);

        long likeCount = hotdealLikeRepository.countByHotDeal(hotDeal);
        long commentCount = hotdealCommentRepository.countByHotDeal(hotDeal);
        List<HotDealCommentResponse> comments = hotdealCommentRepository
                .findByHotDealOrderByCreatedAtDesc(hotDeal)
                .stream()
                .map(HotDealCommentResponse::from)
                .toList();


        return HotDealDetailResponse.from(hotDeal, isLiked, isBookmarked, likeCount, commentCount, comments);
    }

    @Transactional
    public void increaseViewCount(Long hotDealId) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));
        hotDeal.increaseViewCount();
    }

    @Transactional(readOnly = true)
    public PageResponse<HotDealListResponse> getHotDealList(HotDealSearchFilter filter, Pageable pageable) {
        Page<HotDealEntity> hotDeals =
                hotdealRepository.findAll(HotDealSpecification.search(filter), pageable);

        return new PageResponse<>(
                hotDeals.map(h -> {
                    long likeCount = hotdealLikeRepository.countByHotDeal(h);
                    long commentCount = hotdealCommentRepository.countByHotDeal(h);
                    return HotDealListResponse.from(h, likeCount, commentCount);
                })
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<HotDealListResponse> searchHotDeals(String keyword, Pageable pageable) {
        Page<HotDealEntity> hotDeals =
                hotdealRepository.findByTitleContaining(keyword, pageable);

        return new PageResponse<>(
                hotDeals.map(h -> {
                    long likeCount = hotdealLikeRepository.countByHotDeal(h);
                    long commentCount = hotdealCommentRepository.countByHotDeal(h);
                    return HotDealListResponse.from(h, likeCount, commentCount);
                })
        );
    }

    @Transactional
    public void updateHotDeal(Long hotDealId, Long userId, HotDealUpdateRequest request) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));

        if (!hotDeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Region region = regionService.findRegionName(request.getRegionCode());

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

    @Transactional
    public void deleteHotDeal(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));

        if (!hotDeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        hotdealRepository.delete(hotDeal);
    }

    @Transactional
    public void toggleLike(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

        hotdealLikeRepository.findByHotDealAndUser(hotDeal, user)
                .ifPresentOrElse(
                        hotdealLikeRepository::delete,
                        () -> hotdealLikeRepository.save(
                                HotDealLikeEntity.builder()
                                        .hotDeal(hotDeal)
                                        .user(user)
                                        .build()
                        )
                );
    }

    @Transactional
    public void toggleBookmark(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

        hotdealBookmarkRepository.findByHotDealAndUser(hotDeal, user)
                .ifPresentOrElse(
                        hotdealBookmarkRepository::delete,
                        () -> hotdealBookmarkRepository.save(
                                HotDealBookmarkEntity.builder()
                                        .hotDeal(hotDeal)
                                        .user(user)
                                        .build()
                        )
                );
    }

    @Transactional
    public Long createComment(Long hotDealId, Long userId, HotDealCommentCreateRequest request) {
        HotDealEntity hotDeal = hotdealRepository.findById(hotDealId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

        HotDealCommentEntity comment = HotDealCommentEntity.builder()
                .hotDeal(hotDeal)
                .user(user)
                .content(request.getContent())
                .build();

        return hotdealCommentRepository.save(comment).getId();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        HotDealCommentEntity comment = hotdealCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        hotdealCommentRepository.delete(comment);
    }
}
