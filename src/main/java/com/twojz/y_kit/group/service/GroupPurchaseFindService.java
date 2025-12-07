package com.twojz.y_kit.group.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseEntity;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.group.dto.response.GroupPurchaseCommentResponse;
import com.twojz.y_kit.group.dto.response.GroupPurchaseDetailResponse;
import com.twojz.y_kit.group.dto.response.GroupPurchaseListResponse;
import com.twojz.y_kit.group.repository.GroupPurchaseBookmarkRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseCommentRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseLikeRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseParticipantRepository;
import com.twojz.y_kit.group.repository.GroupPurchaseRepository;
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
public class GroupPurchaseFindService {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final GroupPurchaseLikeRepository groupPurchaseLikeRepository;
    private final GroupPurchaseBookmarkRepository groupPurchaseBookmarkRepository;
    private final GroupPurchaseCommentRepository groupPurchaseCommentRepository;
    private final GroupPurchaseParticipantRepository groupPurchaseParticipantRepository;
    private final UserFindService userFindService;

    public GroupPurchaseEntity findById(Long groupPurchaseId) {
        return groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매를 찾을 수 없습니다."));
    }

    @Transactional
    public GroupPurchaseDetailResponse getGroupPurchaseDetail(Long groupPurchaseId, Long userId) {
        GroupPurchaseEntity groupPurchase = findById(groupPurchaseId);

        // 조회수 증가
        groupPurchase.increaseViewCount();

        boolean isLiked = false;
        boolean isBookmarked = false;
        boolean isParticipating = false;

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isLiked = groupPurchaseLikeRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
            isBookmarked = groupPurchaseBookmarkRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
            isParticipating = groupPurchaseParticipantRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchaseId);
        }

        // 엔티티의 카운트 필드 사용
        long likeCount = groupPurchase.getLikeCount();
        long commentCount = groupPurchase.getCommentCount();

        List<GroupPurchaseCommentResponse> comments = groupPurchaseCommentRepository
                .findByGroupPurchaseOrderByCreatedAtDesc(groupPurchase)
                .stream()
                .map(GroupPurchaseCommentResponse::from)
                .toList();

        return GroupPurchaseDetailResponse.from(groupPurchase, isLiked, isBookmarked, isParticipating, likeCount, commentCount, comments);
    }

    // 🔥 userId 매개변수 추가
    public PageResponse<GroupPurchaseListResponse> getGroupPurchaseList(
            GroupPurchaseStatus status,
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        Page<GroupPurchaseEntity> groupPurchases = groupPurchaseRepository
                .findByFilters(status, regionCode, pageable);
        return convertToPageResponse(groupPurchases, userId);
    }

    public PageResponse<GroupPurchaseListResponse> getMyGroupPurchases(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);
        Page<GroupPurchaseEntity> groupPurchases = groupPurchaseRepository.findByUser(user, pageable);
        return convertToPageResponse(groupPurchases, userId);
    }

    public List<GroupPurchaseListResponse> getMyLikedGroupPurchases(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseLikeRepository.findByUser(user)
                .stream()
                .map(like -> {
                    GroupPurchaseEntity groupPurchase = like.getGroupPurchase();
                    boolean isBookmarked = groupPurchaseBookmarkRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
                    boolean isParticipating = groupPurchaseParticipantRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchase.getId());
                    // 🔥 좋아요 목록이므로 isLiked는 항상 true
                    return GroupPurchaseListResponse.from(
                            groupPurchase,
                            true,  // isLiked
                            isBookmarked,
                            isParticipating,
                            groupPurchase.getLikeCount(),
                            groupPurchase.getCommentCount()
                    );
                })
                .toList();
    }

    public List<GroupPurchaseListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseBookmarkRepository.findByUser(user)
                .stream()
                .map(bookmark -> {
                    GroupPurchaseEntity groupPurchase = bookmark.getGroupPurchase();
                    boolean isLiked = groupPurchaseLikeRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
                    boolean isParticipating = groupPurchaseParticipantRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchase.getId());
                    // 🔥 북마크 목록이므로 isBookmarked는 항상 true
                    return GroupPurchaseListResponse.from(
                            groupPurchase,
                            isLiked,
                            true,  // isBookmarked
                            isParticipating,
                            groupPurchase.getLikeCount(),
                            groupPurchase.getCommentCount()
                    );
                })
                .toList();
    }

    public List<GroupPurchaseListResponse> getMyParticipatingGroupPurchases(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseParticipantRepository.findByUser(user)
                .stream()
                .map(participant -> {
                    GroupPurchaseEntity groupPurchase = participant.getGroupPurchase();
                    boolean isLiked = groupPurchaseLikeRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
                    boolean isBookmarked = groupPurchaseBookmarkRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
                    // 🔥 참여 목록이므로 isParticipating은 항상 true
                    return GroupPurchaseListResponse.from(
                            groupPurchase,
                            isLiked,
                            isBookmarked,
                            true,  // isParticipating
                            groupPurchase.getLikeCount(),
                            groupPurchase.getCommentCount()
                    );
                })
                .toList();
    }

    public List<GroupPurchaseCommentResponse> getMyComments(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseCommentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(GroupPurchaseCommentResponse::from)
                .toList();
    }

    /**
     * LIKE + OR를 사용한 통합 검색 (상태/지역 필터 옵션, OR 조건)
     */
    // 🔥 userId 매개변수 추가
    public PageResponse<GroupPurchaseListResponse> searchGroupPurchases(
            String keyword,
            GroupPurchaseStatus status,
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        List<String> extractedKeywords = extractKeywords(keyword);

        Page<GroupPurchaseEntity> groupPurchases = groupPurchaseRepository.searchByKeywords(
                status,
                regionCode,
                getKeywordOrNull(extractedKeywords, 0),
                getKeywordOrNull(extractedKeywords, 1),
                getKeywordOrNull(extractedKeywords, 2),
                getKeywordOrNull(extractedKeywords, 3),
                getKeywordOrNull(extractedKeywords, 4),
                pageable
        );

        return convertToPageResponse(groupPurchases, userId);
    }

    /**
     * 🔥 HotDeal처럼 N+1 문제 해결하면서 좋아요/북마크 여부 포함
     */
    private PageResponse<GroupPurchaseListResponse> convertToPageResponse(
            Page<GroupPurchaseEntity> groupPurchases,
            Long userId
    ) {
        List<GroupPurchaseEntity> groupPurchaseList = groupPurchases.getContent();

        if (groupPurchaseList.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }

        List<Long> groupPurchaseIds = groupPurchaseList.stream()
                .map(GroupPurchaseEntity::getId)
                .toList();

        // 사용자의 좋아요/북마크/참여 여부 일괄 조회
        Set<Long> likedGroupPurchaseIds = new HashSet<>();
        Set<Long> bookmarkedGroupPurchaseIds = new HashSet<>();
        Set<Long> participatingGroupPurchaseIds = new HashSet<>();

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);

            likedGroupPurchaseIds = new HashSet<>(
                    groupPurchaseLikeRepository.findLikedGroupPurchaseIdsByUserAndGroupPurchaseIds(user, groupPurchaseIds)
            );

            bookmarkedGroupPurchaseIds = new HashSet<>(
                    groupPurchaseBookmarkRepository.findBookmarkedGroupPurchaseIdsByUserAndGroupPurchaseIds(user, groupPurchaseIds)
            );

            participatingGroupPurchaseIds = new HashSet<>(
                    groupPurchaseParticipantRepository.findParticipatingGroupPurchaseIdsByUserIdAndGroupPurchaseIds(userId, groupPurchaseIds)
            );
        }

        // Response 생성
        Set<Long> finalLikedIds = likedGroupPurchaseIds;
        Set<Long> finalBookmarkedIds = bookmarkedGroupPurchaseIds;
        Set<Long> finalParticipatingIds = participatingGroupPurchaseIds;

        Page<GroupPurchaseListResponse> page = groupPurchases.map(groupPurchase -> {
            boolean isLiked = finalLikedIds.contains(groupPurchase.getId());
            boolean isBookmarked = finalBookmarkedIds.contains(groupPurchase.getId());
            boolean isParticipating = finalParticipatingIds.contains(groupPurchase.getId());

            return GroupPurchaseListResponse.from(
                    groupPurchase,
                    isLiked,
                    isBookmarked,
                    isParticipating,
                    groupPurchase.getLikeCount(),
                    groupPurchase.getCommentCount()
            );
        });

        return new PageResponse<>(page);
    }

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
                    .toList();

        } catch (Exception e) {
            log.error("형태소 분석 실패: {}", text, e);
            return List.of(text);
        }
    }

    private String getKeywordOrNull(List<String> keywords, int index) {
        return index < keywords.size() ? keywords.get(index) : null;
    }
}