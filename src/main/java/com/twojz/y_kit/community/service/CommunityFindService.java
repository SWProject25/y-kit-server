package com.twojz.y_kit.community.service;

import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.community.dto.response.CommentResponse;
import com.twojz.y_kit.community.dto.response.CommunityDetailResponse;
import com.twojz.y_kit.community.dto.response.CommunityListResponse;
import com.twojz.y_kit.community.repository.CommunityBookmarkRepository;
import com.twojz.y_kit.community.repository.CommunityCommentRepository;
import com.twojz.y_kit.community.repository.CommunityLikeRepository;
import com.twojz.y_kit.community.repository.CommunityRepository;
import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityFindService {
    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityBookmarkRepository communityBookmarkRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserFindService userFindService;

    public CommunityEntity findById(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public CommunityDetailResponse getCommunityDetail(Long communityId, Long userId) {
        CommunityEntity community = findById(communityId);

        UserEntity user = userFindService.findUser(userId);

        boolean isLiked = communityLikeRepository.existsByCommunityAndUser(community, user);
        boolean isBookmarked = communityBookmarkRepository.existsByCommunityAndUser(community, user);
        long likeCount = communityLikeRepository.countByCommunity(community);
        long commentCount = communityCommentRepository.countByCommunity(community);

        List<CommentResponse> comments = communityCommentRepository
                .findByCommunityOrderByCreatedAtDesc(community)
                .stream()
                .map(CommentResponse::from)
                .toList();

        return CommunityDetailResponse.from(community, isLiked, isBookmarked, likeCount, commentCount, comments);
    }

    public PageResponse<CommunityListResponse> getCommunityList(CommunityCategory category, Pageable pageable) {
        Page<CommunityEntity> communities = (category != null)
                ? communityRepository.findByCategory(category, pageable)
                : communityRepository.findAll(pageable);

        Page<CommunityListResponse> page = communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        });

        return new PageResponse<>(page);
    }

    public PageResponse<CommunityListResponse> searchCommunities(String keyword, Pageable pageable) {
        Page<CommunityEntity> communities =
                communityRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);

        Page<CommunityListResponse> page = communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        });

        return new PageResponse<>(page);
    }

    public PageResponse<CommunityListResponse> getMyPosts(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);
        Page<CommunityEntity> communities = communityRepository.findByUser(user, pageable);

        Page<CommunityListResponse> page = communities.map(community -> {
            long likeCount = communityLikeRepository.countByCommunity(community);
            long commentCount = communityCommentRepository.countByCommunity(community);
            return CommunityListResponse.from(community, likeCount, commentCount);
        });

        return new PageResponse<>(page);
    }

    public List<CommunityListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return communityBookmarkRepository.findByUser(user)
                .stream()
                .map(bookmark -> {
                    CommunityEntity community = bookmark.getCommunity();
                    long likeCount = communityLikeRepository.countByCommunity(community);
                    long commentCount = communityCommentRepository.countByCommunity(community);
                    return CommunityListResponse.from(community, likeCount, commentCount);
                })
                .toList();
    }
}