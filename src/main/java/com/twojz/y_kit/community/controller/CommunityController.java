package com.twojz.y_kit.community.controller;

import com.twojz.y_kit.community.domain.vo.CommunityCategory;
import com.twojz.y_kit.community.dto.request.CommentCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityCreateRequest;
import com.twojz.y_kit.community.dto.request.CommunityUpdateRequest;
import com.twojz.y_kit.community.dto.response.CommunityDetailResponse;
import com.twojz.y_kit.community.dto.response.CommunityListResponse;
import com.twojz.y_kit.community.service.CommunityCommandService;
import com.twojz.y_kit.community.service.CommunityFindService;
import com.twojz.y_kit.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityCommandService communityCommandService;
    private final CommunityFindService communityFindService;

    @Operation(summary = "게시글 목록 조회",
            description = "로그인 시 좋아요/북마크 여부 포함")
    @GetMapping
    public ResponseEntity<PageResponse<CommunityListResponse>> getCommunityList(
            Authentication authentication,  // 🔥 추가
            @RequestParam(required = false) CommunityCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserIdOrNull(authentication);  // 🔥 추가
        PageResponse<CommunityListResponse> page = communityFindService.getCommunityList(category, userId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "게시글 상세 조회",
            description = "로그인 시 좋아요/북마크 여부 포함")
    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityDetailResponse> getCommunityDetail(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long communityId,
            Authentication authentication) {

        communityCommandService.increaseViewCount(communityId);

        Long userId = extractUserIdOrNull(authentication);  // 🔥 로그인 선택으로 변경
        CommunityDetailResponse response = communityFindService.getCommunityDetail(communityId, userId);
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
        Long communityId = communityCommandService.createCommunity(userId, request);
        return ResponseEntity.ok(communityId);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{communityId}")
    public ResponseEntity<Void> updateCommunity(
            @PathVariable Long communityId,
            Authentication authentication,
            @Valid @RequestBody CommunityUpdateRequest request) {

        Long userId = extractUserId(authentication);
        communityCommandService.updateCommunity(communityId, userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable Long communityId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        communityCommandService.deleteCommunity(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요 토글", description = "게시글의 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{communityId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long communityId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        communityCommandService.toggleLike(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "북마크 토글", description = "게시글의 북마크를 추가하거나 취소합니다.")
    @PostMapping("/{communityId}/bookmark")
    public ResponseEntity<Void> toggleBookmark(
            @PathVariable Long communityId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        communityCommandService.toggleBookmark(communityId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 작성")
    @PostMapping("/{communityId}/comments")
    public ResponseEntity<Long> createComment(
            @PathVariable Long communityId,
            Authentication authentication,
            @Valid @RequestBody CommentCreateRequest request) {

        Long userId = extractUserId(authentication);
        Long commentId = communityCommandService.createComment(communityId, userId, request);
        return ResponseEntity.ok(commentId);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        communityCommandService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/comments/{commentId}")
    public void editComment(
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @RequestBody CommentCreateRequest request) {

        Long userId = extractUserId(authentication);
        communityCommandService.updateComment(commentId, userId, request);
    }

    @Operation(summary = "내가 작성한 게시글 목록")
    @GetMapping("/my-posts")
    public ResponseEntity<PageResponse<CommunityListResponse>> getMyPosts(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserId(authentication);
        PageResponse<CommunityListResponse> page = communityFindService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "내가 북마크한 게시글 목록")
    @GetMapping("/my-bookmarks")
    public ResponseEntity<List<CommunityListResponse>> getMyBookmarks(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<CommunityListResponse> bookmarks = communityFindService.getMyBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "내가 좋아요한 게시글 목록")
    @GetMapping("/my-liked")
    public ResponseEntity<List<CommunityListResponse>> getMyLiked(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<CommunityListResponse> liked = communityFindService.getMyLiked(userId);
        return ResponseEntity.ok(liked);
    }

    @Operation(summary = "내가 작성한 댓글 목록")
    @GetMapping("/my-comments")
    public ResponseEntity<List<com.twojz.y_kit.community.dto.response.CommentResponse>> getMyComments(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<com.twojz.y_kit.community.dto.response.CommentResponse> comments = communityFindService.getMyComments(userId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "게시글 검색",
            description = "제목 또는 내용으로 게시글을 검색합니다. 로그인 시 좋아요/북마크 여부 포함")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<CommunityListResponse>> searchCommunities(
            Authentication authentication,  // 🔥 추가
            @RequestParam @Size(min = 2, message = "검색어는 최소 2자 이상이어야 합니다") String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserIdOrNull(authentication);  // 🔥 추가
        PageResponse<CommunityListResponse> page = communityFindService.searchCommunities(null, keyword, userId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "카테고리별 검색",
            description = "로그인 시 좋아요/북마크 여부 포함")
    @GetMapping("/search/category")
    public ResponseEntity<PageResponse<CommunityListResponse>> searchByCategory(
            Authentication authentication,  // 🔥 추가
            @RequestParam CommunityCategory category,
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = extractUserIdOrNull(authentication);  // 🔥 추가
        PageResponse<CommunityListResponse> results = communityFindService.searchCommunities(category, keyword, userId, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "실시간 순위", description = "조회수 + 북마크 수 기준으로 상위 5개 게시글을 조회합니다. 데이터가 부족한 경우 무작위로 5개를 선택합니다.")
    @GetMapping("/trending")
    public ResponseEntity<List<CommunityListResponse>> getTrendingCommunities() {
        List<CommunityListResponse> trending = communityFindService.getTrendingCommunities();
        return ResponseEntity.ok(trending);
    }

    /**
     * 🔥 로그인 필수 - userId 추출 (로그인 안되어 있으면 예외 발생)
     */
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

    /**
     * 🔥 로그인 선택 - userId 추출 (로그인 안되어 있으면 null 반환)
     */
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