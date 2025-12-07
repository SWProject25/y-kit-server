package com.twojz.y_kit.community.service;

import com.twojz.y_kit.community.domain.entity.CommunityBookmarkEntity;
import com.twojz.y_kit.community.domain.entity.CommunityCommentEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.entity.CommunityLikeEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.community.dto.request.CommentCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityUpdateRequest;
import com.twojz.y_kit.community.repository.CommunityBookmarkRepository;
import com.twojz.y_kit.community.repository.CommunityCommentRepository;
import com.twojz.y_kit.community.repository.CommunityLikeRepository;
import com.twojz.y_kit.community.repository.CommunityRepository;
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
public class CommunityCommandService {
    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityBookmarkRepository communityBookmarkRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserFindService userFindService;
    private final CommunityFindService communityFindService;
    private final BadgeCommandService badgeCommandService;
    private final BadgeFindService badgeFindService;

    public Long createCommunity(Long userId, CommunityCreateRequest request) {
        UserEntity user = userFindService.findUser(userId);

        // 첫 게시물인지 확인
        long userPostCount = communityRepository.countByUser(user);
        boolean isFirstPost = (userPostCount == 0);

        CommunityEntity community = CommunityEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(CommunityCategory.of(request.getCategory()))
                .user(user)
                .build();

        Long communityId = communityRepository.save(community).getId();

        // 첫 게시물이면 뱃지 부여
        if (isFirstPost) {
            try {
                BadgeEntity badge = badgeFindService.findByName("커뮤니티 첫 글");
                badgeCommandService.grantBadgeIfNotExists(userId, badge.getId());
                log.info("🏅 '커뮤니티 첫 글' 뱃지 부여 완료 - userId: {}", userId);
            } catch (Exception e) {
                log.warn("뱃지 부여 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        }

        return communityId;
    }

    public void updateCommunity(Long communityId, Long userId, CommunityUpdateRequest request) {
        CommunityEntity community = communityFindService.findCommunity(communityId);

        if (!community.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        community.update(
                request.getTitle(),
                request.getContent(),
                CommunityCategory.of(request.getCategory())
        );
    }

    public void deleteCommunity(Long communityId, Long userId) {
        CommunityEntity community = communityFindService.findCommunity(communityId);

        if (!community.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        communityRepository.delete(community);
    }

    public void toggleLike(Long communityId, Long userId) {
        CommunityEntity community = communityFindService.findCommunity(communityId);
        UserEntity user = userFindService.findUser(userId);

        communityLikeRepository.findByCommunityAndUser(community, user)
                .ifPresentOrElse(
                        communityLikeRepository::delete,
                        () -> communityLikeRepository.save(
                                CommunityLikeEntity.builder()
                                        .community(community)
                                        .user(user)
                                        .build()
                        )
                );
    }

    public void toggleBookmark(Long communityId, Long userId) {
        CommunityEntity community = communityFindService.findCommunity(communityId);
        UserEntity user = userFindService.findUser(userId);

        communityBookmarkRepository.findByCommunityAndUser(community, user)
                .ifPresentOrElse(
                        communityBookmarkRepository::delete,
                        () -> communityBookmarkRepository.save(
                                CommunityBookmarkEntity.builder()
                                        .community(community)
                                        .user(user)
                                        .build()
                        )
                );
    }

    public Long createComment(Long communityId, Long userId, CommentCreateRequest request) {
        CommunityEntity community = communityFindService.findCommunity(communityId);
        UserEntity user = userFindService.findUser(userId);

        CommunityCommentEntity comment = CommunityCommentEntity.builder()
                .community(community)
                .user(user)
                .content(request.getContent())
                .build();

        return communityCommentRepository.save(comment).getId();
    }

    public void deleteComment(Long commentId, Long userId) {
        CommunityCommentEntity comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        communityCommentRepository.delete(comment);
    }

    public void updateComment(Long commentId, Long userId, CommentCreateRequest request) {
        CommunityCommentEntity comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        // 내용 수정
        comment.updateContent(request.getContent());
    }

    public void increaseViewCount(Long communityId) {
        CommunityEntity community = communityFindService.findCommunity(communityId);
        community.increaseViewCount();
    }
}