package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.dto.request.BadgeCreateRequest;
import com.twojz.y_kit.user.dto.response.BadgeResponse;
import com.twojz.y_kit.user.dto.request.BadgeUpdateRequest;
import com.twojz.y_kit.user.dto.request.UserBadgeGrantRequest;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.service.AdminBadgeService;
import com.twojz.y_kit.user.service.BadgeCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "관리자 뱃지 관리 API")
@RestController
@RequestMapping("/api/admin/badges")
@RequiredArgsConstructor
public class AdminBadgeController {
    private final AdminBadgeService adminBadgeService;
    private final BadgeCommandService badgeCommandService;

    @Operation(summary = "뱃지 생성")
    @PostMapping
    public ResponseEntity<BadgeResponse> createBadge(@Valid @RequestBody BadgeCreateRequest request) {
        BadgeEntity badge = adminBadgeService.createBadge(
                request.getName(),
                request.getDescription(),
                request.getIconUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BadgeResponse.from(badge));
    }

    @Operation(summary = "뱃지 수정")
    @PutMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> updateBadge(
            @PathVariable Long badgeId,
            @Valid @RequestBody BadgeUpdateRequest request) {
        BadgeEntity badge = adminBadgeService.updateBadge(
                badgeId,
                request.getName(),
                request.getDescription(),
                request.getIconUrl()
        );
        return ResponseEntity.ok(BadgeResponse.from(badge));
    }

    @Operation(summary = "뱃지 삭제")
    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long badgeId) {
        adminBadgeService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 뱃지 목록 조회")
    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeEntity> badges = adminBadgeService.getAllBadges();
        return ResponseEntity.ok(mapToBadgeResponses(badges));
    }

    @Operation(summary = "사용자에게 뱃지 부여")
    @PostMapping("/grant")
    public ResponseEntity<Void> grantBadgeToUser(@Valid @RequestBody UserBadgeGrantRequest request) {
        badgeCommandService.grantBadge(request.getUserId(), request.getBadgeId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자로부터 뱃지 회수")
    @DeleteMapping("/revoke")
    public ResponseEntity<Void> revokeBadgeFromUser(@Valid @RequestBody UserBadgeGrantRequest request) {
        badgeCommandService.revokeBadge(request.getUserId(), request.getBadgeId());
        return ResponseEntity.noContent().build();
    }

    private List<BadgeResponse> mapToBadgeResponses(List<BadgeEntity> badges) {
        return badges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
    }
}