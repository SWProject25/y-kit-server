package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealDetailDto;
import com.twojz.y_kit.hotdeal.domain.dto.HotDealListDto;
import com.twojz.y_kit.hotdeal.domain.entity.DealType;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.dto.response.HotDealCommentResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealDetailResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotDealFindService {
    private final HotDealRepository hotDealRepository;
    private final HotDealCommentRepository hotDealCommentRepository;

    /**
     * ID로 핫딜 조회
     */
    public HotDealEntity findById(Long id) {
        return hotDealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다. id: " + id));
    }

    /**
     * 핫딜 목록 조회 (페이징)
     */
    public Page<HotDealListResponse> getHotDealList(Long userId, Pageable pageable) {
        Page<HotDealListDto> dtos = hotDealRepository.findHotDealList(userId, pageable);
        log.debug("📋 핫딜 목록 조회 - userId: {}, count: {}", userId, dtos.getTotalElements());
        return dtos.map(HotDealListResponse::fromListDto);
    }

    /**
     * 핫딜 검색 (다중 필터)
     */
    public Page<HotDealListResponse> searchHotDeals(
            String keyword,
            DealType dealType,
            HotDealCategory category,
            String regionCode,
            Long userId,
            Pageable pageable
    ) {
        Page<HotDealListDto> dtos = hotDealRepository.searchHotDeals(
                keyword, dealType, category, regionCode, userId, pageable
        );
        log.debug("🔍 핫딜 검색 - keyword: {}, dealType: {}, category: {}, regionCode: {}, count: {}",
                keyword, dealType, category, regionCode, dtos.getTotalElements());
        return dtos.map(HotDealListResponse::fromListDto);
    }

    /**
     * 핫딜 상세 조회
     */
    public HotDealDetailResponse getHotDealDetail(Long hotDealId, Long userId) {
        // 상세 DTO 조회
        HotDealDetailDto dto = hotDealRepository.findHotDealDetail(hotDealId, userId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다. id: " + hotDealId));

        // 댓글 조회 (Entity 조회 필요)
        HotDealEntity hotDeal = findById(hotDealId);
        List<HotDealCommentResponse> comments = hotDealCommentRepository
                .findByHotDealOrderByCreatedAtDesc(hotDeal)
                .stream()
                .map(HotDealCommentResponse::from)
                .toList();

        log.debug("📄 핫딜 상세 조회 - hotDealId: {}, userId: {}, commentCount: {}",
                hotDealId, userId, comments.size());

        return HotDealDetailResponse.fromDetailDto(dto, comments);
    }

    /**
     * 좋아요한 핫딜 목록
     */
    public Page<HotDealListResponse> getLikedHotDeals(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        Page<HotDealListDto> dtos = hotDealRepository.findLikedHotDeals(userId, pageable);
        log.debug("❤️ 좋아요 핫딜 목록 - userId: {}, count: {}", userId, dtos.getTotalElements());
        return dtos.map(HotDealListResponse::fromListDto);
    }

    /**
     * 북마크한 핫딜 목록
     */
    public Page<HotDealListResponse> getBookmarkedHotDeals(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        Page<HotDealListDto> dtos = hotDealRepository.findBookmarkedHotDeals(userId, pageable);
        log.debug("📌 북마크 핫딜 목록 - userId: {}, count: {}", userId, dtos.getTotalElements());
        return dtos.map(HotDealListResponse::fromListDto);
    }

    /**
     * 내가 작성한 핫딜 목록
     */
    public Page<HotDealListResponse> getMyHotDeals(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        Page<HotDealListDto> dtos = hotDealRepository.findMyHotDeals(userId, pageable);
        log.debug("✍️ 내가 작성한 핫딜 - userId: {}, count: {}", userId, dtos.getTotalElements());
        return dtos.map(HotDealListResponse::fromListDto);
    }
}