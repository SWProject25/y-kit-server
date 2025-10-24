package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/v1/user/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody LocalSignUpRequest request) {
        userService.saveLocalUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/auth/login/kakao")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    @GetMapping("/v1/auth/oauth2/success")
    public ResponseEntity<Map<String, String>> oauthSuccess(
            @RequestParam String accessToken,
            @RequestParam String refreshToken
    ) {
        log.info("🎉 OAuth2 로그인 완료!");
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);

        return ResponseEntity.ok(Map.of(
                "message", "OAuth2 로그인 성공",
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
}