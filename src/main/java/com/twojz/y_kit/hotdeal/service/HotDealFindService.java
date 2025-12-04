package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.dto.response.HotDealCommentResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealDetailResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.hotdeal.repository.HotDealBookmarkRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealLikeRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.util.List;
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
public class HotDealFindService {
    private final HotDealRepository hotDealRepository;
    private final HotDealLikeRepository hotDealLikeRepository;
    private final HotDealBookmarkRepository hotDealBookmarkRepository;
    private final HotDealCommentRepository hotDealCommentRepository;
    private final UserFindService userFindService;

    public HotDealEntity findById(Long id) {
        return hotDealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다. id: " + id));
    }

    public PageResponse<HotDealListResponse> getHotDealList(HotDealCategory category, Long userId, Pageable pageable) {
        Page<HotDealEntity> hotDeals = (category != null)
                ? hotDealRepository.findByCategory(category, pageable)
                : hotDealRepository.findAll(pageable);

        return convertToPageResponse(hotDeals, userId);
    }

    public HotDealDetailResponse getHotDealDetail(Long hotDealId, Long userId) {
        HotDealEntity hotDeal = findById(hotDealId);

        boolean isLiked = false;
        boolean isBookmarked = false;

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            isLiked = hotDealLikeRepository.existsByHotDealAndUser(hotDeal, user);
            isBookmarked = hotDealBookmarkRepository.existsByHotDealAndUser(hotDeal, user);
        }

        long likeCount = hotDealLikeRepository.countByHotDeal(hotDeal);
        long commentCount = hotDealCommentRepository.countByHotDeal(hotDeal);

        List<HotDealCommentResponse> comments = hotDealCommentRepository
                .findByHotDealOrderByCreatedAtDesc(hotDeal)
                .stream()
                .map(HotDealCommentResponse::from)
                .toList();

        return HotDealDetailResponse.from(hotDeal, isLiked, isBookmarked, likeCount, commentCount, comments);
    }

    /**
     * 내가 작성한 핫딜 목록
     */
    public PageResponse<HotDealListResponse> getMyHotDeals(Long userId, Pageable pageable) {
        UserEntity user = userFindService.findUser(userId);
        Page<HotDealEntity> hotDeals = hotDealRepository.findByUser(user, pageable);

        return convertToPageResponse(hotDeals, userId);
    }

    /**
     * 좋아요한 핫딜 목록
     */
    public List<HotDealListResponse> getMyLikedHotDeals(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return hotDealLikeRepository.findByUser(user)
                .stream()
                .map(like -> {
                    HotDealEntity hotDeal = like.getHotDeal();
                    long likeCount = hotDealLikeRepository.countByHotDeal(hotDeal);
                    long commentCount = hotDealCommentRepository.countByHotDeal(hotDeal);
                    boolean isBookmarked = hotDealBookmarkRepository.existsByHotDealAndUser(hotDeal, user);
                    return HotDealListResponse.from(hotDeal, likeCount, commentCount, true, isBookmarked);
                })
                .toList();
    }

    /**
     * 북마크한 핫딜 목록
     */
    public List<HotDealListResponse> getMyBookmarks(Long userId) {
        UserEntity user = userFindService.findUser(userId);

        return hotDealBookmarkRepository.findByUser(user)
                .stream()
                .map(bookmark -> {
                    HotDealEntity hotDeal = bookmark.getHotDeal();
                    long likeCount = hotDealLikeRepository.countByHotDeal(hotDeal);
                    long commentCount = hotDealCommentRepository.countByHotDeal(hotDeal);
                    boolean isLiked = hotDealLikeRepository.existsByHotDealAndUser(hotDeal, user);
                    return HotDealListResponse.from(hotDeal, likeCount, commentCount, isLiked, true);
                })
                .toList();
    }

    /**
     * LIKE + OR를 사용한 통합 검색 (카테고리, 딜타입 필터 옵션, OR 조건)
     */
    public PageResponse<HotDealListResponse> searchHotDeals(
            HotDealCategory category,
            DealType dealType,
            String keyword,
            Long userId,
            Pageable pageable
    ) {
        List<String> extractedKeywords = extractKeywords(keyword);

        Page<HotDealEntity> hotDeals = hotDealRepository.searchByKeywords(
                category,
                dealType,
                getKeywordOrNull(extractedKeywords, 0),
                getKeywordOrNull(extractedKeywords, 1),
                getKeywordOrNull(extractedKeywords, 2),
                getKeywordOrNull(extractedKeywords, 3),
                getKeywordOrNull(extractedKeywords, 4),
                pageable
        );

        return convertToPageResponse(hotDeals, userId);
    }

    /**
     * Entity를 PageResponse로 변환 (N+1 문제 해결 + 좋아요/북마크 여부 포함)
     */
    private PageResponse<HotDealListResponse> convertToPageResponse(Page<HotDealEntity> hotDeals, Long userId) {
        List<HotDealEntity> hotDealList = hotDeals.getContent();

        if (hotDealList.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }

        List<Long> hotDealIds = hotDealList.stream()
                .map(HotDealEntity::getId)
                .toList();

        // 좋아요 수 일괄 조회
        java.util.Map<Long, Long> likeCountMap = hotDealLikeRepository.countByHotDealIds(hotDealIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 댓글 수 일괄 조회
        java.util.Map<Long, Long> commentCountMap = hotDealCommentRepository.countByHotDealIds(hotDealIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 사용자의 좋아요/북마크 여부 일괄 조회
        java.util.Set<Long> likedHotDealIds = new java.util.HashSet<>();
        java.util.Set<Long> bookmarkedHotDealIds = new java.util.HashSet<>();

        if (userId != null) {
            UserEntity user = userFindService.findUser(userId);
            likedHotDealIds = new java.util.HashSet<>(
                    hotDealLikeRepository.findLikedHotDealIdsByUserAndHotDealIds(user, hotDealIds)
            );
            bookmarkedHotDealIds = new java.util.HashSet<>(
                    hotDealBookmarkRepository.findBookmarkedHotDealIdsByUserAndHotDealIds(user, hotDealIds)
            );
        }

        // Response 생성
        java.util.Set<Long> finalLikedHotDealIds = likedHotDealIds;
        java.util.Set<Long> finalBookmarkedHotDealIds = bookmarkedHotDealIds;

        Page<HotDealListResponse> page = hotDeals.map(hotDeal -> {
            long likeCount = likeCountMap.getOrDefault(hotDeal.getId(), 0L);
            long commentCount = commentCountMap.getOrDefault(hotDeal.getId(), 0L);
            boolean isLiked = finalLikedHotDealIds.contains(hotDeal.getId());
            boolean isBookmarked = finalBookmarkedHotDealIds.contains(hotDeal.getId());
            return HotDealListResponse.from(hotDeal, likeCount, commentCount, isLiked, isBookmarked);
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