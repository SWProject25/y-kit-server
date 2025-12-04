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
import java.util.List;
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

    public GroupPurchaseDetailResponse getGroupPurchaseDetail(Long groupPurchaseId, Long userId) {
        GroupPurchaseEntity groupPurchase = findById(groupPurchaseId);

        boolean isLiked = false;
        boolean isBookmarked = false;
        boolean isParticipating = false;

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isLiked = groupPurchaseLikeRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
            isBookmarked = groupPurchaseBookmarkRepository.existsByGroupPurchaseAndUser(groupPurchase, user);
            isParticipating = groupPurchaseParticipantRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchaseId);
        }

        long likeCount = groupPurchaseLikeRepository.countByGroupPurchase(groupPurchase);
        long commentCount = groupPurchaseCommentRepository.countByGroupPurchase(groupPurchase);

        List<GroupPurchaseCommentResponse> comments = groupPurchaseCommentRepository
                .findByGroupPurchaseOrderByCreatedAtDesc(groupPurchase)
                .stream()
                .map(GroupPurchaseCommentResponse::from)
                .toList();

        return GroupPurchaseDetailResponse.from(groupPurchase, isLiked, isBookmarked, isParticipating, likeCount, commentCount, comments);
    }

    public PageResponse<GroupPurchaseListResponse> getGroupPurchaseList(
            GroupPurchaseStatus status,
            String regionCode,
            Pageable pageable
    ) {
        Page<GroupPurchaseEntity> groupPurchases = groupPurchaseRepository
                .findByFilters(status, regionCode, pageable);
        return convertToPageResponse(groupPurchases);
    }

    public PageResponse<GroupPurchaseListResponse> getMyGroupPurchases(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);
        Page<GroupPurchaseEntity> groupPurchases = groupPurchaseRepository.findByUser(user, pageable);
        return convertToPageResponse(groupPurchases);
    }

    public List<GroupPurchaseListResponse> getMyLikedGroupPurchases(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseLikeRepository.findByUser(user)
                .stream()
                .map(like -> {
                    GroupPurchaseEntity groupPurchase = like.getGroupPurchase();
                    long likeCount = groupPurchaseLikeRepository.countByGroupPurchase(groupPurchase);
                    long commentCount = groupPurchaseCommentRepository.countByGroupPurchase(groupPurchase);
                    return GroupPurchaseListResponse.from(groupPurchase, likeCount, commentCount);
                })
                .toList();
    }

    public List<GroupPurchaseListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseBookmarkRepository.findByUser(user)
                .stream()
                .map(bookmark -> {
                    GroupPurchaseEntity groupPurchase = bookmark.getGroupPurchase();
                    long likeCount = groupPurchaseLikeRepository.countByGroupPurchase(groupPurchase);
                    long commentCount = groupPurchaseCommentRepository.countByGroupPurchase(groupPurchase);
                    return GroupPurchaseListResponse.from(groupPurchase, likeCount, commentCount);
                })
                .toList();
    }

    public List<GroupPurchaseListResponse> getMyParticipatingGroupPurchases(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return groupPurchaseParticipantRepository.findByUser(user)
                .stream()
                .map(participant -> {
                    GroupPurchaseEntity groupPurchase = participant.getGroupPurchase();
                    long likeCount = groupPurchaseLikeRepository.countByGroupPurchase(groupPurchase);
                    long commentCount = groupPurchaseCommentRepository.countByGroupPurchase(groupPurchase);
                    return GroupPurchaseListResponse.from(groupPurchase, likeCount, commentCount);
                })
                .toList();
    }

    /**
     * LIKE + OR를 사용한 통합 검색 (상태/지역 필터 옵션, OR 조건)
     */
    public PageResponse<GroupPurchaseListResponse> searchGroupPurchases(String keyword, GroupPurchaseStatus status, String regionCode, Pageable pageable) {
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

        return convertToPageResponse(groupPurchases);
    }

    private PageResponse<GroupPurchaseListResponse> convertToPageResponse(Page<GroupPurchaseEntity> groupPurchases) {
        List<GroupPurchaseEntity> groupPurchaseList = groupPurchases.getContent();

        if (groupPurchaseList.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }

        List<Long> groupPurchaseIds = groupPurchaseList.stream()
                .map(GroupPurchaseEntity::getId)
                .toList();

        java.util.Map<Long, Long> likeCountMap = groupPurchaseLikeRepository.countByGroupPurchaseIds(groupPurchaseIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        java.util.Map<Long, Long> commentCountMap = groupPurchaseCommentRepository.countByGroupPurchaseIds(groupPurchaseIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        Page<GroupPurchaseListResponse> page = groupPurchases.map(groupPurchase -> {
            long likeCount = likeCountMap.getOrDefault(groupPurchase.getId(), 0L);
            long commentCount = commentCountMap.getOrDefault(groupPurchase.getId(), 0L);
            return GroupPurchaseListResponse.from(groupPurchase, likeCount, commentCount);
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