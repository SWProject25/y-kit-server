package com.twojz.y_kit.group.controller;

import com.twojz.y_kit.global.dto.PageResponse;
import com.twojz.y_kit.group.domain.entity.GroupPurchaseStatus;
import com.twojz.y_kit.group.dto.request.*;
import com.twojz.y_kit.group.dto.response.*;
import com.twojz.y_kit.group.service.GroupPurchaseCommandService;
import com.twojz.y_kit.group.service.GroupPurchaseFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공동구매 API")
@RestController
@RequestMapping("/api/v1/group-purchases")
@RequiredArgsConstructor
public class GroupPurchaseController {
    private final GroupPurchaseCommandService groupPurchaseCommandService;
    private final GroupPurchaseFindService groupPurchaseFindService;

    @PostMapping
    @Operation(summary = "공동구매 생성")
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
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.getGroupPurchaseDetail(id, userId);
    }

    @GetMapping
    @Operation(summary = "공동구매 목록 조회")
    public PageResponse<GroupPurchaseListResponse> getGroupPurchases(
            @RequestParam(required = false) GroupPurchaseStatus status,
            @RequestParam(required = false) String regionCode,
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserId(authentication);

        if (status != null && regionCode != null) {
            return groupPurchaseFindService.getGroupPurchasesByStatusAndRegion(
                    status, regionCode, userId, pageable
            );
        }

        if (status != null) {
            return groupPurchaseFindService.getGroupPurchasesByStatus(status, userId, pageable);
        }

        if (regionCode != null) {
            return groupPurchaseFindService.getGroupPurchasesByRegion(regionCode, userId, pageable);
        }

        return groupPurchaseFindService.getGroupPurchaseList(userId, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "공동구매 검색")
    public PageResponse<GroupPurchaseListResponse> searchGroupPurchases(
            @RequestParam String keyword,
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = extractUserId(authentication);
        return groupPurchaseFindService.searchGroupPurchases(keyword, userId, pageable);
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

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("잘못된 사용자 정보입니다.", e);
        }
    }
}