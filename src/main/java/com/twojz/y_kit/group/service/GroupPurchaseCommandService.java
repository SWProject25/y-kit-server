package com.twojz.y_kit.group.service;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseBookmarkEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseCategory;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseCommentEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseLikeEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseParticipantEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.group.dto.request.GroupPurchaseCommentCreateRequest;
import com.twojz.y_kit.group.dto.request.GroupPurchaseCreateRequest;
import com.twojz.y_kit.group.dto.request.GroupPurchaseUpdateRequest;
import com.twojz.y_kit.group.repository.GroupPurchaseBookmarkRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseCommentRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseLikeRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseParticipantRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseRepository;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.service.RegionFindService;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.BadgeCommandService;
import com.twojz.y_kit.user.service.BadgeFindService;
import com.twojz.y_kit.user.service.UserFindService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupPurchaseCommandService {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final GroupPurchaseLikeRepository likeRepository;
    private final GroupPurchaseBookmarkRepository bookmarkRepository;
    private final GroupPurchaseCommentRepository commentRepository;
    private final GroupPurchaseParticipantRepository participantRepository;
    private final UserFindService userFindService;
    private final RegionFindService regionFindService;
    private final GroupPurchaseFindService groupPurchaseFindService;
    private final BadgeCommandService badgeCommandService;
    private final BadgeFindService badgeFindService;

    public Long createGroupPurchase(Long userId, GroupPurchaseCreateRequest request) {
        UserEntity user = userFindService.findUser(userId);

        // 첫 게시물인지 확인
        long userPostCount = groupPurchaseRepository.countByUser(user);
        boolean isFirstPost = (userPostCount == 0);

        // 지역 정보 결정: 주소 기반 검색
        Region region = regionFindService.findRegionByAddress(
                request.getSido(),
                request.getSigungu(),
                request.getDong()
        );

        if (request.getMinParticipants() > request.getMaxParticipants()) {
            throw new IllegalArgumentException("최소 참여 인원은 최대 참여 인원보다 클 수 없습니다.");
        }

        // String을 GroupPurchaseCategory로 변환
        GroupPurchaseCategory category = null;
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            try {
                category = GroupPurchaseCategory.valueOf(request.getCategory());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + request.getCategory());
            }
        }

        GroupPurchaseEntity gp = GroupPurchaseEntity.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .productName(request.getProductName())
                .productLink(request.getProductLink())
                .contact(request.getContact())
                .price(request.getPrice())
                .minParticipants(request.getMinParticipants())
                .maxParticipants(request.getMaxParticipants())
                .deadline(request.getDeadline())
                .region(region)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .category(category)
                .build();

        Long groupPurchaseId = groupPurchaseRepository.save(gp).getId();

        // 첫 게시물이면 뱃지 부여
        if (isFirstPost) {
            try {
                BadgeEntity badge = badgeFindService.findByName("공동구매 첫 개설");
                badgeCommandService.grantBadgeIfNotExists(userId, badge.getId());
                log.info("🏅 '공동구매 첫 개설' 뱃지 부여 완료 - userId: {}", userId);
            } catch (Exception e) {
                log.warn("뱃지 부여 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        }

        return groupPurchaseId;
    }

    public void updateGroupPurchase(Long groupPurchaseId, Long userId, GroupPurchaseUpdateRequest request) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(groupPurchaseId);

        if (!gp.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        if (request.getMinParticipants() > request.getMaxParticipants()) {
            throw new IllegalArgumentException("최소 참여 인원은 최대 참여 인원보다 클 수 없습니다.");
        }

        Region region = regionFindService.findRegionByAddress(
                request.getSido(),
                request.getSigungu(),
                request.getDong()
        );

        // String을 GroupPurchaseCategory로 변환
        GroupPurchaseCategory category = null;
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            try {
                category = GroupPurchaseCategory.valueOf(request.getCategory());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + request.getCategory());
            }
        }

        gp.update(
                request.getTitle(),
                request.getContent(),
                request.getProductName(),
                request.getProductLink(),
                request.getContact(),
                request.getPrice(),
                request.getMinParticipants(),
                request.getMaxParticipants(),
                request.getDeadline(),
                region,
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress(),
                category
        );
    }

    public void deleteGroupPurchase(Long groupPurchaseId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(groupPurchaseId);

        if (!gp.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        likeRepository.deleteByGroupPurchase(gp);
        bookmarkRepository.deleteByGroupPurchase(gp);
        commentRepository.deleteByGroupPurchase(gp);
        participantRepository.deleteByGroupPurchase(gp);

        groupPurchaseRepository.delete(gp);
    }

    public void toggleLike(Long gpId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        likeRepository.findByGroupPurchaseAndUser(gp, user)
                .ifPresentOrElse(
                        like -> {
                            likeRepository.delete(like);
                            gp.decreaseLikeCount();
                        },
                        () -> {
                            likeRepository.save(
                                    GroupPurchaseLikeEntity.builder()
                                            .groupPurchase(gp)
                                            .user(user)
                                            .build()
                            );
                            gp.increaseLikeCount();
                        }
                );
    }

    public void toggleBookmark(Long gpId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        bookmarkRepository.findByGroupPurchaseAndUser(gp, user)
                .ifPresentOrElse(
                        bookmark -> {
                            bookmarkRepository.delete(bookmark);
                            gp.decreaseBookmarkCount();
                        },
                        () -> {
                            bookmarkRepository.save(
                                    GroupPurchaseBookmarkEntity.builder()
                                            .groupPurchase(gp)
                                            .user(user)
                                            .build()
                            );
                            gp.increaseBookmarkCount();
                        }
                );
    }

    public Long createComment(Long gpId, Long userId, GroupPurchaseCommentCreateRequest request) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        GroupPurchaseCommentEntity comment = GroupPurchaseCommentEntity.builder()
                .groupPurchase(gp)
                .user(user)
                .content(request.getContent())
                .build();

        Long commentId = commentRepository.save(comment).getId();

        // 댓글 수 증가
        gp.increaseCommentCount();

        return commentId;
    }

    public void deleteComment(Long commentId, Long userId) {
        GroupPurchaseCommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        GroupPurchaseEntity gp = comment.getGroupPurchase();
        commentRepository.delete(comment);

        // 댓글 수 감소
        gp.decreaseCommentCount();
    }

    public void joinGroupPurchase(Long gpId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        if (gp.getStatus() != GroupPurchaseStatus.OPEN) {
            throw new IllegalArgumentException("모집이 마감된 공동구매입니다.");
        }

        if (LocalDate.now().isAfter(gp.getDeadline())) {
            throw new IllegalArgumentException("마감일이 지난 공동구매입니다.");
        }

        if (gp.getCurrentParticipants() >= gp.getMaxParticipants()) {
            throw new IllegalArgumentException("참여 인원이 모두 찼습니다.");
        }

        boolean alreadyJoined = participantRepository.findByGroupPurchaseAndUser(gp, user).isPresent();
        if (alreadyJoined) {
            throw new IllegalArgumentException("이미 참여했습니다.");
        }

        participantRepository.save(
                GroupPurchaseParticipantEntity.builder()
                        .groupPurchase(gp)
                        .user(user)
                        .build()
        );

        gp.increaseParticipants();
    }
}
