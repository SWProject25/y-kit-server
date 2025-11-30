package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.auth.JwtTokenProvider;
import com.twojz.y_kit.user.dto.request.DeviceTokenRequest;
import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.dto.request.LoginRequest;
import com.twojz.y_kit.user.dto.request.ProfileCompleteRequest;
import com.twojz.y_kit.user.dto.response.UserResponse;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "회원관리 API")
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserFindService userFindService;
    private final UserDeviceService userDeviceService;
    private final UserNotificationService userNotificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로컬 회원가입
     */
    @Operation(summary = "로컬 회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody LocalSignUpRequest request) {
        userService.saveLocalUser(request);

        UserEntity user = userFindService.findUser(request.getEmail());

        userNotificationService.sendWelcomeNotification(user);

        if (user.getProfileStatus() != ProfileStatus.COMPLETED) {
            userNotificationService.sendProfileCompleteReminder(user);
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

        String token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("profileStatus", user.getProfileStatus());
        response.put("needProfileComplete", user.getProfileStatus() != ProfileStatus.COMPLETED);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 추가 정보 입력
     */
    @Operation(summary = "프로필 추가 정보 입력")
    @PostMapping("/profile/complete")
    public ResponseEntity<Void> completeProfile(
            Authentication authentication,
            @RequestBody ProfileCompleteRequest request) {

        Long userId = extractUserId(authentication);
        userService.completeProfile(userId, request);

        UserEntity user = userFindService.findUser(userId);

        userNotificationService.sendProfileCompletedNotification(user);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        Long userId = extractUserId(authentication);
        UserEntity user = userFindService.findUser(userId);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .region(user.getRegion() != null ? user.getRegion().getName() : null)
                .profileStatus(user.getProfileStatus())
                .build();

        return ResponseEntity.ok(response);
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
            @RequestBody DeviceTokenRequest request) {
        Long userId = extractUserId(authentication);
        userDeviceService.enableNotification(userId, request.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 끄기 (특정 디바이스)")
    @PutMapping("/notification/disable")
    public ResponseEntity<Void> disableNotification(
            Authentication authentication,
            @RequestBody DeviceTokenRequest request) {
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