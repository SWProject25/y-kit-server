package com.twojz.y_kit.user.auth;

import com.twojz.y_kit.user.entity.ProfileStatus;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserRepository;
import com.twojz.y_kit.user.service.UserNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;

    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = extractEmail(oAuth2User);
            Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                        .queryParam("needSignup", true)
                        .queryParam("email", email)
                        .build()
                        .toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }

            UserEntity user = optionalUser.get();

            // 신규 가입 여부 확인 (createdAt과 updatedAt이 같으면 신규 가입)
            boolean isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());

            // JWT 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(),
                    user.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

            // 신규 가입 시에만 알림 전송
            if (isNewUser) {
                userNotificationService.sendWelcomeNotification(user);

                if (user.getProfileStatus() != ProfileStatus.COMPLETED
                        && user.getProfileStatus() != ProfileStatus.SKIPPED) {
                    userNotificationService.sendProfileCompleteReminder(user);
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

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {

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