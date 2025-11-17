package com.twojz.y_kit.hotdeal.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.dto.request.*;
import com.twojz.y_kit.hotdeal.dto.response.*;
import com.twojz.y_kit.hotdeal.service.HotDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    public Long createHotDeal(Authentication authentication, @RequestBody HotDealCreateRequest request) {
        Long userId = extractUserId(authentication);
        return hotDealService.createHotDeal(userId, request);
    }

    @GetMapping("/{hotDealId}")
    @Operation(summary = "핫딜 상세 조회")
    public HotDealDetailResponse getHotDeal(@PathVariable Long hotDealId,Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealService.increaseViewCount(hotDealId);
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
    public void updateHotDeal(@PathVariable Long id, Authentication authentication, @RequestBody HotDealUpdateRequest request) {
        Long userId = extractUserId(authentication);
        hotDealService.updateHotDeal(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "핫딜 삭제")
    public void deleteHotDeal(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealService.deleteHotDeal(id, userId);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글")
    public void toggleLike(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealService.toggleLike(id, userId);
    }

    @PostMapping("/{id}/bookmark")
    @Operation(summary = "북마크 토글")
    public void toggleBookmark(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealService.toggleBookmark(id, userId);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "댓글 작성")
    public Long createComment(@PathVariable Long id, Authentication authentication, @RequestBody HotDealCommentCreateRequest request) {
        Long userId = extractUserId(authentication);
        return hotDealService.createComment(id, userId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public void deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealService.deleteComment(commentId, userId);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 사용자 정보입니다.", e);
        }
    }
}