package com.twojz.y_kit.group.service;

import com.twojz.y_kit.group.domain.entity.GroupPurchaseBookmarkEntity;
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
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Long createGroupPurchase(Long userId, GroupPurchaseCreateRequest request) {
        UserEntity user = userFindService.findUser(userId);
        Region region = regionFindService.findRegionCode(request.getRegionCode());

        if (request.getMinParticipants() > request.getMaxParticipants()) {
            throw new IllegalArgumentException("최소 참여 인원은 최대 참여 인원보다 클 수 없습니다.");
        }

        GroupPurchaseEntity gp = GroupPurchaseEntity.builder()
                .user(user)
                .title(request.getTitle())
                .productName(request.getProductName())
                .productLink(request.getProductLink())
                .price(request.getPrice())
                .minParticipants(request.getMinParticipants())
                .maxParticipants(request.getMaxParticipants())
                .deadline(request.getDeadline())
                .status(request.getStatus())
                .region(region)
                .build();

        return groupPurchaseRepository.save(gp).getId();
    }

    public void updateGroupPurchase(Long groupPurchaseId, Long userId, GroupPurchaseUpdateRequest request) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(groupPurchaseId);

        if (!gp.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        if (request.getMinParticipants() > request.getMaxParticipants()) {
            throw new IllegalArgumentException("최소 참여 인원은 최대 참여 인원보다 클 수 없습니다.");
        }

        Region region = regionFindService.findRegionCode(request.getRegionCode());

        gp.update(
                request.getTitle(),
                request.getProductName(),
                request.getProductLink(),
                request.getPrice(),
                request.getMinParticipants(),
                request.getMaxParticipants(),
                request.getDeadline(),
                request.getStatus(),
                region
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
                        likeRepository::delete,
                        () -> likeRepository.save(
                                GroupPurchaseLikeEntity.builder()
                                        .groupPurchase(gp)
                                        .user(user)
                                        .build()
                        )
                );
    }

    public void toggleBookmark(Long gpId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        bookmarkRepository.findByGroupPurchaseAndUser(gp, user)
                .ifPresentOrElse(
                        bookmarkRepository::delete,
                        () -> bookmarkRepository.save(
                                GroupPurchaseBookmarkEntity.builder()
                                        .groupPurchase(gp)
                                        .user(user)
                                        .build()
                        )
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

        return commentRepository.save(comment).getId();
    }

    public void deleteComment(Long commentId, Long userId) {
        GroupPurchaseCommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    public void joinGroupPurchase(Long gpId, Long userId) {
        GroupPurchaseEntity gp = groupPurchaseFindService.findById(gpId);
        UserEntity user = userFindService.findUser(userId);

        if (gp.getStatus() != GroupPurchaseStatus.OPEN) {
            throw new IllegalArgumentException("모집이 마감된 공동구매입니다.");
        }

        if (LocalDateTime.now().isAfter(gp.getDeadline())) {
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
