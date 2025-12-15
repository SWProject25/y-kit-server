package com.twojz.y_kit.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserRepository;
import com.twojz.y_kit.user.service.UserNotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;

    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @PostConstruct
    public void init() {
        log.info("🚀 [OAuth2] OAuth2SuccessHandler 초기화 완료");
        log.info("🚀 [OAuth2] redirectUrl: {}", redirectUrl);
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("🟢 [OAuth2] onAuthenticationSuccess 시작");
        log.info("🟢 [OAuth2] redirectUrl 설정값: {}", redirectUrl);

        try {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            log.info("🟢 [OAuth2] OAuth2User attributes: {}", oAuth2User.getAttributes());

            String email = extractEmail(oAuth2User);
            log.info("🟢 [OAuth2] 추출된 이메일: {}", email);

            Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

            // 회원가입이 안되어 있으면 → 회원가입 필요 상태로 리다이렉트
            if (optionalUser.isEmpty()) {
                log.info("⚠️ [OAuth2] 미가입 사용자 - 회원가입 필요: {}", email);

                String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                        .queryParam("needSignup", true)
                        .queryParam("email", email)
                        .build()
                        .toUriString();

                log.info("🟢 [OAuth2] 회원가입 페이지로 리다이렉트: {}", targetUrl);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }

        UserEntity user = optionalUser.get();
        log.info("🟢 [OAuth2] 사용자 조회 성공 - userId: {}, email: {}, profileStatus: {}",
                user.getId(), user.getEmail(), user.getProfileStatus());

        // 신규 가입 여부 확인 (createdAt과 updatedAt이 같으면 신규 가입)
        boolean isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
        log.info("🟢 [OAuth2] 신규 가입 여부: {}", isNewUser);

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        log.info("🟢 [OAuth2] JWT 토큰 생성 완료");

        // 신규 가입 시에만 알림 전송
        if (isNewUser) {
            try {
                log.info("🟢 [OAuth2] 신규 가입자 알림 전송 시작");
                userNotificationService.sendWelcomeNotification(user);

                if (user.getProfileStatus() != ProfileStatus.COMPLETED
                        && user.getProfileStatus() != ProfileStatus.SKIPPED) {
                    userNotificationService.sendProfileCompleteReminder(user);
                }
                log.info("🟢 [OAuth2] 신규 가입자 알림 전송 완료");
            } catch (Exception e) {
                log.error("❌ [OAuth2] 알림 전송 실패", e);
            }
        }

        // 쿼리 파라미터로 토큰 전달
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("profileStatus", user.getProfileStatus())
                .queryParam("isNewUser", isNewUser)
                .queryParam("needProfileComplete", user.getProfileStatus() != ProfileStatus.COMPLETED)
                .build()
                .toUriString();

        log.info("🟢 [OAuth2] 최종 리다이렉트 URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
        log.info("🟢 [OAuth2] onAuthenticationSuccess 완료");
        } catch (Exception e) {
            log.error("❌ [OAuth2] onAuthenticationSuccess 실패", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(DefaultOAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 로그인
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }

        return (String) attributes.get("email");
    }
}