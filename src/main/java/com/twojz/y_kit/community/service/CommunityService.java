package com.twojz.y_kit.community.service;

import com.twojz.y_kit.community.domain.entity.*;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.community.dto.request.*;
import com.twojz.y_kit.community.dto.response.*;
import com.twojz.y_kit.community.repository.*;
import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityBookmarkRepository communityBookmarkRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserService userService;

    @Transactional
    public Long createCommunity(Long userId, CommunityCreateRequest request) {
        UserEntity user = userService.findUser(userId);

        CommunityEntity community = CommunityEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(CommunityCategory.of(request.getCategory()))
                .user(user)
                .build();

        return communityRepository.save(community).getId();
    }

    @Transactional
    public CommunityDetailResponse getCommunityDetail(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        community.increaseViewCount();

        UserEntity user = userId != null ? userService.findUser(userId) : null;

        boolean isLiked = user != null && communityLikeRepository.existsByCommunityAndUser(community, user);
        boolean isBookmarked = user != null && communityBookmarkRepository.existsByCommunityAndUser(community, user);
        long likeCount = communityLikeRepository.countByCommunity(community);
        long commentCount = communityCommentRepository.countByCommunity(community);

        return CommunityDetailResponse.from(community, isLiked, isBookmarked, likeCount, commentCount);
    }

    public PageResponse<CommunityListResponse> getCommunityList(CommunityCategory category, Pageable pageable) {
        Page<CommunityEntity> communities = (category != null)
                ? communityRepository.findByCategory(category, pageable)
                : communityRepository.findAll(pageable);

        return new PageResponse<>(communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        }));
    }

    public PageResponse<CommunityListResponse> searchCommunities(String keyword, Pageable pageable) {
        Page<CommunityEntity> communities =
                communityRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);

        return new PageResponse<>(communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        }));
    }

    @Transactional
    public void updateCommunity(Long communityId, Long userId, CommunityUpdateRequest request) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!community.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        community.update(request.getTitle(), request.getContent(), CommunityCategory.of(request.getCategory()));
    }

    @Transactional
    public void deleteCommunity(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!community.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        communityRepository.delete(community);
    }

    @Transactional
    public void toggleLike(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

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

    @Transactional
    public void toggleBookmark(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

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

    @Transactional
    public Long createComment(Long communityId, Long userId, CommentCreateRequest request) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        UserEntity user = userService.findUser(userId);

        CommunityCommentEntity comment = CommunityCommentEntity.builder()
                .community(community)
                .user(user)
                .content(request.getContent())
                .build();

        return communityCommentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(Long communityId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<CommunityCommentEntity> comments =
                communityCommentRepository.findByCommunityOrderByCreatedAtDesc(community);

        return comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommunityCommentEntity comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        communityCommentRepository.delete(comment);
    }

    public PageResponse<CommunityListResponse> getMyPosts(Long userId, Pageable pageable) {
        UserEntity user = userService.findUser(userId);
        Page<CommunityEntity> communities = communityRepository.findByUser(user, pageable);

        Page<CommunityListResponse> mappedPage = communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        });

        return new PageResponse<>(mappedPage);
    }

    public List<CommunityListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userService.findUser(userId);
        List<CommunityBookmarkEntity> bookmarks = communityBookmarkRepository.findByUser(user);

        return bookmarks.stream()
                .map(bookmark -> {
                    CommunityEntity community = bookmark.getCommunity();
                    long likeCount = communityLikeRepository.countByCommunity(community);
                    long commentCount = communityCommentRepository.countByCommunity(community);
                    return CommunityListResponse.from(community, likeCount, commentCount);
                })
                .toList();
    }
}