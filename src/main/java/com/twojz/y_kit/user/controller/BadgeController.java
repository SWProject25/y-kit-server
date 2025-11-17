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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Badge 조회 API")
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
        List<BadgeResponse> responses = badges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "뱃지 상세 조회", description = "특정 뱃지의 상세 정보를 조회합니다.")
    @GetMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> getBadge(@PathVariable Long badgeId) {
        BadgeEntity badge = badgeFindService.findBadge(badgeId);
        return ResponseEntity.ok(BadgeResponse.from(badge));
    }

    @Operation(summary = "내 뱃지 조회", description = "현재 로그인한 사용자가 보유한 뱃지 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges(
            @RequestHeader("X-User-Id") Long userId) { // 실제로는 Security Context에서 가져오는 것이 좋습니다
        List<UserBadgeEntity> userBadges = userBadgeFindService.getUserBadges(userId);
        List<UserBadgeResponse> responses = userBadges.stream()
                .map(UserBadgeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "특정 사용자의 뱃지 조회", description = "특정 사용자가 보유한 뱃지 목록을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(@PathVariable Long userId) {
        List<UserBadgeEntity> userBadges = userBadgeFindService.getUserBadges(userId);
        List<UserBadgeResponse> responses = userBadges.stream()
                .map(UserBadgeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}