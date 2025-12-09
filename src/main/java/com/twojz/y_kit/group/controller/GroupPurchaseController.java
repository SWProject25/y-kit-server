package com.twojz.y_kit.group.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseCategory;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.group.dto.request.*;
import com.twojz.y_kit.group.dto.response.*;
import com.twojz.y_kit.group.service.GroupPurchaseCommandService;
import com.twojz.y_kit.group.service.GroupPurchaseFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "공동구매 API")
@RestController
@RequestMapping("/api/v1/group-purchases")
@RequiredArgsConstructor
public class GroupPurchaseController {
    private final GroupPurchaseCommandService groupPurchaseCommandService;
    private final GroupPurchaseFindService groupPurchaseFindService;

    @PostMapping
    @Operation(summary = "공동구매 생성")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createGroupPurchase(
            Authentication authentication,
            @RequestBody GroupPurchaseCreateRequest request
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseCommandService.createGroupPurchase(userId, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "공동구매 상세 조회")
    public GroupPurchaseDetailResponse getGroupPurchase(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return groupPurchaseFindService.getGroupPurchaseDetail(id, userId);
    }

    @GetMapping
    @Operation(summary = "공동구매 목록 조회 (전체/상태/지역/카테고리 필터)",
            description = "로그인 시 좋아요/북마크/참여 여부 포함")
    public PageResponse<GroupPurchaseListResponse> getGroupPurchases(
            Authentication authentication,
            @RequestParam(required = false) GroupPurchaseStatus status,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) GroupPurchaseCategory category,
            Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return groupPurchaseFindService.getGroupPurchaseList(status, regionCode, category, userId, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "공동구매 통합 검색 (형태소 분석 + 상태/지역/카테고리 필터)",
            description = "로그인 시 좋아요/북마크/참여 여부 포함")
    public PageResponse<GroupPurchaseListResponse> searchGroupPurchases(
            Authentication authentication,
            @RequestParam String keyword,
            @RequestParam(required = false) GroupPurchaseStatus status,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) GroupPurchaseCategory category,
            Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);
        return groupPurchaseFindService.searchGroupPurchases(keyword, status, regionCode, category, userId, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "공동구매 수정")
    public void updateGroupPurchase(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody GroupPurchaseUpdateRequest request
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.updateGroupPurchase(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공동구매 삭제")
    public void deleteGroupPurchase(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.deleteGroupPurchase(id, userId);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "좋아요 토글")
    public void toggleLike(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.toggleLike(id, userId);
    }

    @PostMapping("/{id}/bookmark")
    @Operation(summary = "북마크 토글")
    public void toggleBookmark(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.toggleBookmark(id, userId);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "댓글 작성")
    public Long createComment(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody GroupPurchaseCommentCreateRequest request
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseCommandService.createComment(id, userId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.deleteComment(commentId, userId);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public void editComment(
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestBody GroupPurchaseCommentCreateRequest request
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.updateComment(commentId, userId, request);
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "공동구매 참여")
    public void joinGroupPurchase(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        groupPurchaseCommandService.joinGroupPurchase(id, userId);
    }

    @GetMapping("/my-purchases")
    @Operation(summary = "내가 생성한 공동구매 목록")
    public PageResponse<GroupPurchaseListResponse> getMyGroupPurchases(
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getMyGroupPurchases(userId, pageable);
    }

    @GetMapping("/my-bookmarks")
    @Operation(summary = "내가 북마크한 공동구매 목록")
    public java.util.List<GroupPurchaseListResponse> getMyBookmarks(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getMyBookmarks(userId);
    }

    @GetMapping("/my-liked")
    @Operation(summary = "내가 좋아요한 공동구매 목록")
    public java.util.List<GroupPurchaseListResponse> getMyLiked(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getMyLikedGroupPurchases(userId);
    }

    @GetMapping("/my-comments")
    @Operation(summary = "내가 작성한 댓글 목록")
    public java.util.List<GroupPurchaseCommentResponse> getMyComments(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getMyComments(userId);
    }

    @GetMapping("/my-participations")
    @Operation(summary = "내가 참여한 공동구매 목록")
    public java.util.List<GroupPurchaseListResponse> getMyParticipations(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getMyParticipatingGroupPurchases(userId);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 사용자 정보입니다.", e);
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