package com.twojz.y_kit.hotdeal.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.hotdeal.domain.entity.HotDealCategory;
import com.twojz.y_kit.hotdeal.dto.request.*;
import com.twojz.y_kit.hotdeal.dto.response.*;
import com.twojz.y_kit.hotdeal.service.HotDealCommandService;
import com.twojz.y_kit.hotdeal.service.HotDealFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "동네핫딜 API")
@RestController
@RequestMapping("/api/v1/hotDeals")
@RequiredArgsConstructor
public class HotDealController {
    private final HotDealCommandService hotDealCommandService;
    private final HotDealFindService hotDealFindService;

    @PostMapping
    @Operation(summary = "핫딜 생성")
    public Long createHotDeal(Authentication authentication, @RequestBody HotDealCreateRequest request) {
        Long userId = extractUserId(authentication);
        return hotDealCommandService.createHotDeal(userId, request);
    }

    @GetMapping("/{hotDealId}")
    @Operation(summary = "핫딜 상세 조회")
    public HotDealDetailResponse getHotDeal(
            @PathVariable Long hotDealId,
            Authentication authentication
    ) {
        hotDealCommandService.increaseViewCount(hotDealId);
        Long userId = extractUserId(authentication);
        return hotDealFindService.getHotDealDetail(hotDealId, userId);
    }

    @GetMapping
    @Operation(summary = "핫딜 목록 조회")
    public PageResponse<HotDealListResponse> getHotDeals(
            @RequestParam(required = false) HotDealCategory category,
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return hotDealFindService.getHotDealList(category, userId, pageable);
    }


    @PutMapping("/{id}")
    @Operation(summary = "핫딜 수정")
    public void updateHotDeal(@PathVariable Long id, Authentication authentication, @RequestBody HotDealUpdateRequest request) {
        Long userId = extractUserId(authentication);
        hotDealCommandService.updateHotDeal(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "핫딜 삭제")
    public void deleteHotDeal(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealCommandService.deleteHotDeal(id, userId);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글")
    public void toggleLike(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealCommandService.toggleLike(id, userId);
    }

    @PostMapping("/{id}/bookmark")
    @Operation(summary = "북마크 토글")
    public void toggleBookmark(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealCommandService.toggleBookmark(id, userId);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "댓글 작성")
    public Long createComment(@PathVariable Long id, Authentication authentication, @RequestBody HotDealCommentCreateRequest request) {
        Long userId = extractUserId(authentication);
        return hotDealCommandService.createComment(id, userId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public void deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        hotDealCommandService.deleteComment(commentId, userId);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public void editComment(@PathVariable Long commentId, Authentication authentication, @RequestBody HotDealCommentCreateRequest request)  {
        Long userId = extractUserId(authentication);
        hotDealCommandService.updateComment(commentId, userId, request);
    }

    @GetMapping("/search")
    @Operation(summary = "핫딜 검색", description = "제목, 내용, 장소명으로 핫딜을 검색합니다")
    public PageResponse<HotDealListResponse> searchHotDeals(
            @RequestParam(required = false) HotDealCategory category,
            @RequestParam(required = false) String keyword,
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return hotDealFindService.searchHotDeals(category, null, keyword, userId, pageable);
    }

    @GetMapping("/my-posts")
    @Operation(summary = "내가 작성한 핫딜 목록")
    public PageResponse<HotDealListResponse> getMyPosts(
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserId(authentication);
        return hotDealFindService.getMyHotDeals(userId, pageable);
    }

    @GetMapping("/my-bookmarks")
    @Operation(summary = "내가 북마크한 핫딜 목록")
    public java.util.List<HotDealListResponse> getMyBookmarks(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return hotDealFindService.getMyBookmarks(userId);
    }

    @GetMapping("/my-liked")
    @Operation(summary = "내가 좋아요한 핫딜 목록")
    public java.util.List<HotDealListResponse> getMyLiked(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return hotDealFindService.getMyLikedHotDeals(userId);
    }

    @GetMapping("/my-comments")
    @Operation(summary = "내가 작성한 댓글 목록")
    public java.util.List<HotDealCommentResponse> getMyComments(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return hotDealFindService.getMyComments(userId);
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

    private Long extractUserIdOrNull(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal.toString())) {
            return null;
        }

        String name = null;
        try {
            name = authentication.getName();
        } catch (Exception e) {
            return null;
        }

        if (name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }

        try {
            return Long.parseLong(name.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}