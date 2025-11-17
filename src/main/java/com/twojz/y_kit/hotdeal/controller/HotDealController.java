package com.twojz.y_kit.hotdeal.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.dto.request.*;
import com.twojz.y_kit.hotdeal.dto.response.*;
import com.twojz.y_kit.hotdeal.service.HotDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "동네핫딜 API")
@RestController
@RequestMapping("/api/v1/hotDeals")
@RequiredArgsConstructor
public class HotDealController {
    private final HotDealService hotDealService;

    @PostMapping
    @Operation(summary = "핫딜 생성")
    public Long createHotDeal(@RequestParam Long userId, @RequestBody HotDealCreateRequest request) {
        return hotDealService.createHotDeal(userId, request);
    }

    @GetMapping("/{hotDealId}")
    @Operation(summary = "핫딜 상세 조회")
    public HotDealDetailResponse getHotDeal(@PathVariable Long hotDealId, @RequestParam(required = false) Long userId) {
        return hotDealService.getHotDealDetail(hotDealId, userId);
    }

    @GetMapping
    @Operation(summary = "핫딜 목록 조회")
    public PageResponse<HotDealListResponse> getHotDeals(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        if (keyword != null) {
            return hotDealService.searchHotDeals(keyword, pageable);
        }
        return hotDealService.getHotDealList(null, pageable);
    }


    @PutMapping("/{id}")
    @Operation(summary = "핫딜 수정")
    public void updateHotDeal(@PathVariable Long id, @RequestParam Long userId, @RequestBody HotDealUpdateRequest request) {
        hotDealService.updateHotDeal(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "핫딜 삭제")
    public void deleteHotDeal(@PathVariable Long id, @RequestParam Long userId) {
        hotDealService.deleteHotDeal(id, userId);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글")
    public void toggleLike(@PathVariable Long id, @RequestParam Long userId) {
        hotDealService.toggleLike(id, userId);
    }

    @PostMapping("/{id}/bookmark")
    @Operation(summary = "북마크 토글")
    public void toggleBookmark(@PathVariable Long id, @RequestParam Long userId) {
        hotDealService.toggleBookmark(id, userId);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "댓글 작성")
    public Long createComment(@PathVariable Long id, @RequestParam Long userId, @RequestBody HotDealCommentCreateRequest request) {
        return hotDealService.createComment(id, userId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public void deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        hotDealService.deleteComment(commentId, userId);
    }
}