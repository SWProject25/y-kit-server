package com.twojz.y_kit.community.service;

import com.twojz.y_kit.community.domain.entity.CommunityBookmarkEntity;
import com.twojz.y_kit.community.domain.entity.CommunityEntity;
import com.twojz.y_kit.community.domain.entity.CommunityLikeEntity;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityFindService {
    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityBookmarkRepository communityBookmarkRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserFindService userFindService;

    public CommunityEntity findCommunity(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public CommunityDetailResponse getCommunityDetail(Long communityId, Long userId) {
        CommunityEntity community = findCommunity(communityId);

        boolean isLiked = false;
        boolean isBookmarked = false;

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isLiked = communityLikeRepository.existsByCommunityAndUser(community, user);
            isBookmarked = communityBookmarkRepository.existsByCommunityAndUser(community, user);
        }

        long likeCount = communityLikeRepository.countByCommunity(community);
        long commentCount = communityCommentRepository.countByCommunity(community);

        List<CommentResponse> comments = communityCommentRepository
                .findByCommunityOrderByCreatedAtDesc(community)
                .stream()
                .map(CommentResponse::from)
                .toList();

        return CommunityDetailResponse.from(community, isLiked, isBookmarked, likeCount, commentCount, comments);
    }

    // 🔥 userId 매개변수 추가
    public PageResponse<CommunityListResponse> getCommunityList(
            CommunityCategory category,
            Long userId,
            Pageable pageable
    ) {
        Page<CommunityEntity> communities = (category != null)
                ? communityRepository.findByCategory(category, pageable)
                : communityRepository.findAll(pageable);

        return convertToPageResponse(communities, userId);
    }

    public PageResponse<CommunityListResponse> getMyPosts(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);
        Page<CommunityEntity> communities = communityRepository.findByUser(user, pageable);

        return convertToPageResponse(communities, userId);
    }

    public List<CommunityListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return communityBookmarkRepository.findByUser(user)
                .stream()
                .map(bookmark -> {
                    CommunityEntity community = bookmark.getCommunity();
                    boolean isLiked = communityLikeRepository.existsByCommunityAndUser(community, user);
                    long likeCount = communityLikeRepository.countByCommunity(community);
                    long commentCount = communityCommentRepository.countByCommunity(community);
                    // 🔥 북마크 목록이므로 isBookmarked는 항상 true
                    return CommunityListResponse.from(community, isLiked, true, likeCount, commentCount);
                })
                .toList();
    }

    public List<CommunityListResponse> getMyLiked(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return communityLikeRepository.findByUser(user)
                .stream()
                .map(like -> {
                    CommunityEntity community = like.getCommunity();
                    boolean isBookmarked = communityBookmarkRepository.existsByCommunityAndUser(community, user);
                    long likeCount = communityLikeRepository.countByCommunity(community);
                    long commentCount = communityCommentRepository.countByCommunity(community);
                    // 🔥 좋아요 목록이므로 isLiked는 항상 true
                    return CommunityListResponse.from(community, true, isBookmarked, likeCount, commentCount);
                })
                .toList();
    }

    public List<CommentResponse> getMyComments(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return communityCommentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    /**
     * LIKE + OR를 사용한 통합 검색 메서드
     */
    // 🔥 userId 매개변수 추가
    public PageResponse<CommunityListResponse> searchCommunities(
            CommunityCategory category,
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCommunityList(category, userId, pageable);
        }

        List<String> extractedKeywords = extractKeywords(keyword);

        Page<CommunityEntity> communities = communityRepository.searchByKeywords(
                category,
                getKeywordOrNull(extractedKeywords, 0),
                getKeywordOrNull(extractedKeywords, 1),
                getKeywordOrNull(extractedKeywords, 2),
                getKeywordOrNull(extractedKeywords, 3),
                getKeywordOrNull(extractedKeywords, 4),
                pageable
        );

        return convertToPageResponse(communities, userId);
    }

    /**
     * 실시간 순위 조회 (조회수 + 북마크 수 기준, 최대 5개)
     */
    public List<CommunityListResponse> getTrendingCommunities() {
        final int TRENDING_SIZE = 5;
        final int MIN_DATA_THRESHOLD = 10;

        long totalCount = communityRepository.count();

        List<CommunityEntity> communities;
        if (totalCount < MIN_DATA_THRESHOLD) {
            communities = communityRepository.findRandomCommunities(TRENDING_SIZE);
        } else {
            communities = communityRepository.findTrendingCommunities(
                    org.springframework.data.domain.PageRequest.of(0, TRENDING_SIZE)
            );
        }

        return communities.stream()
                .map(community -> {
                    long likeCount = communityLikeRepository.countByCommunity(community);
                    long commentCount = communityCommentRepository.countByCommunity(community);
                    // 🔥 비로그인 상태로 조회
                    return CommunityListResponse.from(community, false, false, likeCount, commentCount);
                })
                .toList();
    }

    /**
     * 🔥 Entity를 PageResponse로 변환 (N+1 문제 해결 + 좋아요/북마크 여부 포함)
     */
    private PageResponse<CommunityListResponse> convertToPageResponse(
            Page<CommunityEntity> communities,
            Long userId
    ) {
        List<CommunityEntity> communityList = communities.getContent();

        if (communityList.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }

        List<Long> communityIds = communityList.stream()
                .map(CommunityEntity::getId)
                .toList();

        // 좋아요 수 일괄 조회
        Map<Long, Long> likeCountMap = communityLikeRepository.countByCommunityIds(communityIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 댓글 수 일괄 조회
        Map<Long, Long> commentCountMap = communityCommentRepository.countByCommunityIds(communityIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 사용자의 좋아요/북마크 여부 일괄 조회
        Set<Long> likedCommunityIds = new HashSet<>();
        Set<Long> bookmarkedCommunityIds = new HashSet<>();

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            likedCommunityIds = new HashSet<>(
                    communityLikeRepository.findLikedCommunityIdsByUserAndCommunityIds(user, communityIds)
            );
            bookmarkedCommunityIds = new HashSet<>(
                    communityBookmarkRepository.findBookmarkedCommunityIdsByUserAndCommunityIds(user, communityIds)
            );
        }

        // Response 생성
        Set<Long> finalLikedIds = likedCommunityIds;
        Set<Long> finalBookmarkedIds = bookmarkedCommunityIds;

        Page<CommunityListResponse> page = communities.map(community -> {
            long likeCount = likeCountMap.getOrDefault(community.getId(), 0L);
            long commentCount = commentCountMap.getOrDefault(community.getId(), 0L);
            boolean isLiked = finalLikedIds.contains(community.getId());
            boolean isBookmarked = finalBookmarkedIds.contains(community.getId());

            return CommunityListResponse.from(community, isLiked, isBookmarked, likeCount, commentCount);
        });

        return new PageResponse<>(page);
    }

    /**
     * 형태소 분석을 통해 의미있는 키워드 추출
     */
    private List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        try {
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
            Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
            return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens)
                    .stream()
                    .filter(keyword -> keyword.length() > 1)
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("형태소 분석 실패: {}", text, e);
            return List.of(text);
        }
    }

    /**
     * 리스트에서 인덱스의 값을 가져오거나 null 반환
     */
    private String getKeywordOrNull(List<String> keywords, int index) {
        return index < keywords.size() ? keywords.get(index) : null;
    }
}