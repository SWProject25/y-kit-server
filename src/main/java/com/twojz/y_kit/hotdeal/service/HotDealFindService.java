package com.twojz.y_kit.hotdeal.service;

import com.twojz.y_kit.hotdeal.domain.dto.HotDealWithCountsDto;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealEntity;
import com.twojz.y_kit.hotdeal.dto.response.HotDealCommentResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealDetailResponse;
import com.twojz.y_kit.hotdeal.dto.response.HotDealListResponse;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotDealFindService {
    private final HotDealRepository hotDealRepository;
    private final HotDealCommentRepository hotDealCommentRepository;

    public HotDealEntity findById(Long id) {
        return hotDealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));
    }

    public Page<HotDealListResponse> getHotDealList(Long userId, Pageable pageable) {
        Page<HotDealWithCountsDto> dtos = hotDealRepository.findHotDealsWithCounts(userId, pageable);
        return dtos.map(HotDealListResponse::fromDto);
    }

    public Page<HotDealListResponse> searchHotDeals(String keyword, Long userId, Pageable pageable) {
        Page<HotDealWithCountsDto> dtos = hotDealRepository.findHotDealsWithCountsByKeyword(keyword, userId, pageable);
        return dtos.map(HotDealListResponse::fromDto);
    }

    public List<HotDealListResponse> getHotDeals(Long userId) {
        return hotDealRepository.findHotDealsWithCounts(userId)
                .stream()
                .map(HotDealListResponse::fromDto)
                .toList();
    }

    public HotDealDetailResponse getHotDealDetail(Long hotDealId, Long userId) {
        HotDealWithCountsDto dto = hotDealRepository.findHotDealWithCountsById(hotDealId, userId)
                .orElseThrow(() -> new IllegalArgumentException("핫딜을 찾을 수 없습니다."));

        HotDealEntity hotDeal = findById(hotDealId);

        var comments = hotDealCommentRepository.findByHotDealOrderByCreatedAtDesc(hotDeal)
                .stream()
                .map(HotDealCommentResponse::from)
                .toList();

        return HotDealDetailResponse.fromDto(dto, comments);
    }
}