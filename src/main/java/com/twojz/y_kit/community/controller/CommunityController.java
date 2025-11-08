package com.twojz.y_kit.community.controller;

import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.community.dto.request.CommentCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityUpdateRequest;
import com.twojz.y_kit.community.dto.response.CommentResponse;
import com.twojz.y_kit.community.dto.response.CommunityDetailResponse;
import com.twojz.y_kit.community.dto.response.CommunityListResponse;
import com.twojz.y_kit.community.service.CommunityService;
import com.twojz.y_kit.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<CommunityListResponse>> getCommunityList(
            @RequestParam(required = false) CommunityCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<CommunityListResponse> page = communityService.getCommunityList(category, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityDetailResponse> getCommunityDetail(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long communityId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        CommunityDetailResponse response = communityService.getCommunityDetail(communityId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<Long> createCommunity(
            Authentication authentication,
            @Valid @RequestBody CommunityCreateRequest request) {
        Long userId = extractUserId(authentication);
        Long communityId = communityService.createCommunity(userId, request);
        return ResponseEntity.ok(communityId);
    }

    @Operation(summary = "게시글 검색", description = "제목 또는 내용으로 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<CommunityListResponse>> searchCommunities(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<CommunityListResponse> page = communityService.searchCommunities(keyword, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{communityId}")
    public ResponseEntity<Void> updateCommunity(
            @PathVariable Long communityId,
            Authentication authentication,
            @Valid @RequestBody CommunityUpdateRequest request) {
        Long userId = extractUserId(authentication);
        communityService.updateCommunity(communityId, userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable Long communityId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        communityService.deleteCommunity(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요 토글", description = "게시글의 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{communityId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long communityId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        communityService.toggleLike(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "북마크 토글", description = "게시글의 북마크를 추가하거나 취소합니다.")
    @PostMapping("/{communityId}/bookmark")
    public ResponseEntity<Void> toggleBookmark(
            @PathVariable Long communityId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        communityService.toggleBookmark(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 작성")
    @PostMapping("/{communityId}/comments")
    public ResponseEntity<Long> createComment(
            @PathVariable Long communityId,
            Authentication authentication,
            @Valid @RequestBody CommentCreateRequest request) {
        Long userId = extractUserId(authentication);
        Long commentId = communityService.createComment(communityId, userId, request);
        return ResponseEntity.ok(commentId);
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/{communityId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long communityId) {
        List<CommentResponse> response = communityService.getComments(communityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        communityService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
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