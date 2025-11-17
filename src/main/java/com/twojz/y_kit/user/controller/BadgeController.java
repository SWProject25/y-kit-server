package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.dto.response.BadgeResponse;
import com.twojz.y_kit.user.dto.response.UserBadgeResponse;
import com.twojz.y_kit.user.entity.BadgeEntity;
import com.twojz.y_kit.user.entity.UserBadgeEntity;
import com.twojz.y_kit.user.service.BadgeFindService;
import com.twojz.y_kit.user.service.UserBadgeFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "뱃지 조회 API")
@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
public class BadgeController {
    private final BadgeFindService badgeFindService;
    private final UserBadgeFindService userBadgeFindService;

    @Operation(summary = "모든 뱃지 조회", description = "등록된 모든 뱃지 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeEntity> badges = badgeFindService.findAllBadges();
        return ResponseEntity.ok(mapToBadgeResponses(badges));
    }

    @Operation(summary = "뱃지 상세 조회", description = "특정 뱃지의 상세 정보를 조회합니다.")
    @GetMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> getBadge(@PathVariable Long badgeId) {
        BadgeEntity badge = badgeFindService.findBadge(badgeId);
        return ResponseEntity.ok(BadgeResponse.from(badge));
    }

    @Operation(summary = "내 뱃지 조회", description = "현재 로그인한 사용자가 보유한 뱃지 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<UserBadgeEntity> userBadges = userBadgeFindService.getUserBadges(userId);
        return ResponseEntity.ok(mapToUserBadgeResponses(userBadges));
    }

    @Operation(summary = "특정 사용자의 뱃지 조회", description = "특정 사용자가 보유한 뱃지 목록을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(@PathVariable Long userId) {
        List<UserBadgeEntity> userBadges = userBadgeFindService.getUserBadges(userId);
        return ResponseEntity.ok(mapToUserBadgeResponses(userBadges));
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

    private List<BadgeResponse> mapToBadgeResponses(List<BadgeEntity> badges) {
        return badges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
    }

    private List<UserBadgeResponse> mapToUserBadgeResponses(List<UserBadgeEntity> userBadges) {
        return userBadges.stream()
                .map(UserBadgeResponse::from)
                .collect(Collectors.toList());
    }
}