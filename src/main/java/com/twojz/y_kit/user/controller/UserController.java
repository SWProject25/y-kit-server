package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.auth.JwtTokenProvider;
import com.twojz.y_kit.user.dto.request.DeviceTokenRequest;
import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.dto.request.LoginRequest;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserDeviceService;
import com.twojz.y_kit.user.service.UserFindService;
import com.twojz.y_kit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "로컬 회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody LocalSignUpRequest request) {
        userService.saveLocalUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로컬 로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userFindService.findUser(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "FCM 디바이스 토큰 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(summary = "FCM 디바이스 토큰 비활성화")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 비활성화 성공")
    })
    @PostMapping("/device/deactivate")
    public ResponseEntity<Void> deactivateDeviceToken(
            Authentication authentication,
            @RequestBody DeviceTokenRequest request) {
        Long userId = extractUserId(authentication);
        userDeviceService.deactivateDevice(userId, request.getDeviceToken());
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