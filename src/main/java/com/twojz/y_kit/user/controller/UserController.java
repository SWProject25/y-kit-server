package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.auth.JwtTokenProvider;
import com.twojz.y_kit.user.dto.request.DeviceTokenRequest;
import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.dto.request.LoginRequest;
import com.twojz.y_kit.user.dto.request.ProfileCompleteRequest;
import com.twojz.y_kit.user.dto.response.UserDeviceResponse;
import com.twojz.y_kit.user.dto.response.UserResponse;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.RefreshTokenService;
import com.twojz.y_kit.user.service.UserDeleteService;
import com.twojz.y_kit.user.service.UserDeviceService;
import com.twojz.y_kit.user.service.UserFindService;
import com.twojz.y_kit.user.service.UserNotificationService;
import com.twojz.y_kit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Tag(name = "회원관리 API")
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserFindService userFindService;
    private final UserDeviceService userDeviceService;
    private final UserDeleteService userDeleteService;
    private final UserNotificationService userNotificationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로컬 회원가입
     */
    @Operation(summary = "로컬 회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody LocalSignUpRequest request) {
        UserEntity user = userService.saveLocalUser(request);

        try {
            userNotificationService.sendWelcomeNotification(user);
        } catch (Exception e) {
            log.warn("환영 알림 전송 실패 : {}", user.getId(), e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 로컬 로그인
     */
    @Operation(summary = "로컬 로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userFindService.findUser(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("profileStatus", user.getProfileStatus());
        response.put("needProfileComplete", user.getProfileStatus() != ProfileStatus.COMPLETED);

        // 디바이스 토큰이 있으면 디바이스 등록 및 리프레시 토큰 발급
        if (request.getDeviceToken() != null && !request.getDeviceToken().isEmpty()) {
            // 디바이스 등록/업데이트 (최대 3개 기기 제한 적용)
            userDeviceService.registerOrUpdateDevice(
                    user.getId(),
                    request.getDeviceName(),
                    request.getDeviceToken()
            );

            // 리프레시 토큰 생성 및 디바이스에 저장
            String refreshToken = refreshTokenService.createAndSaveRefreshToken(user.getId(), request.getDeviceToken());
            response.put("refreshToken", refreshToken);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     */
    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리프레시 토큰이 필요합니다.");
        }

        // 리프레시 토큰 검증 및 사용자 ID 추출
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);

        // 사용자 정보 조회
        UserEntity user = userFindService.findUser(userId);

        // 새 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        Map<String, String> response = new HashMap<>();
        response.put("token", newAccessToken);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 추가 정보 입력
     */
    @Operation(summary = "프로필 추가 정보 입력")
    @PostMapping("/profile/complete")
    public ResponseEntity<Void> completeProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileCompleteRequest request) {

        Long userId = extractUserId(authentication);
        userService.completeProfile(userId, request);

        UserEntity user = userFindService.findUser(userId);

        try {
            userNotificationService.sendProfileCompletedNotification(user);
        } catch (Exception e) {
            log.warn("프로필 완료 알림 전송 실패 - userId: {}", user.getId(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 입력 나중에 하기 (스킵)
     */
    @Operation(summary = "프로필 입력 나중에 하기")
    @PostMapping("/profile/skip")
    public ResponseEntity<Void> skipProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        userService.skipProfile(userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        Long userId = extractUserId(authentication);
        UserEntity user = userFindService.findUserWithRegion(userId);
        UserResponse response = UserResponse.from(user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로필 수정")
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileCompleteRequest request) {

        Long userId = extractUserId(authentication);
        userService.completeProfile(userId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdrawUser(Authentication authentication) {
        Long userId = extractUserId(authentication);
        userDeleteService.withdrawUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "FCM 디바이스 토큰 등록")
    @PostMapping("/device/register")
    public ResponseEntity<Void> registerDeviceToken(
            Authentication authentication,
            @Valid @RequestBody DeviceTokenRequest request) {

        Long userId = extractUserId(authentication);
        userDeviceService.registerOrUpdateDevice(
                userId,
                request.getDeviceName(),
                request.getDeviceToken()
        );

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "FCM 디바이스 토큰 비활성화 (로그아웃)")
    @PostMapping("/device/deactivate")
    public ResponseEntity<Void> deactivateDeviceToken(
            Authentication authentication,
            @RequestBody DeviceTokenRequest request) {
        Long userId = extractUserId(authentication);
        userDeviceService.deactivateDevice(userId, request.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 켜기 (특정 디바이스)")
    @PutMapping("/notification/enable")
    public ResponseEntity<Void> enableNotification(
            Authentication authentication,
            @Valid @RequestBody DeviceTokenRequest request) {
        Long userId = extractUserId(authentication);
        userDeviceService.enableNotification(userId, request.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 끄기 (특정 디바이스)")
    @PutMapping("/notification/disable")
    public ResponseEntity<Void> disableNotification(
            Authentication authentication,
            @Valid @RequestBody DeviceTokenRequest request) {
        Long userId = extractUserId(authentication);
        userDeviceService.disableNotification(userId, request.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 디바이스 알림 켜기")
    @PostMapping("/notification/enable-all")
    public ResponseEntity<Void> enableAllNotifications(Authentication authentication) {
        Long userId = extractUserId(authentication);
        userDeviceService.enableAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 디바이스 알림 끄기")
    @PostMapping("/notification/disable-all")
    public ResponseEntity<Void> disableAllNotifications(Authentication authentication) {
        Long userId = extractUserId(authentication);
        userDeviceService.disableAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 디바이스 목록 조회")
    @GetMapping("/devices")
    public ResponseEntity<java.util.List<UserDeviceResponse>> getMyDevices(Authentication authentication) {
        Long userId = extractUserId(authentication);
        java.util.List<UserDeviceResponse> devices = userDeviceService.getMyDevices(userId);
        return ResponseEntity.ok(devices);
    }

    @Operation(summary = "디바이스 삭제 (강제 로그아웃)")
    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Void> removeDevice(
            Authentication authentication,
            @PathVariable Long deviceId) {
        Long userId = extractUserId(authentication);
        userDeviceService.removeDevice(userId, deviceId);
        return ResponseEntity.ok().build();
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
}