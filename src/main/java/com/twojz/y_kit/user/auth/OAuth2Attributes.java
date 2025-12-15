package com.twojz.y_kit.user.auth;

import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
public class OAuth2Attributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String socialId;
    private String name;
    private String email;
    private LoginProvider loginProvider;

    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        log.info("🟡 [OAuth2Attributes] of 시작 - registrationId: {}", registrationId);

        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }

        log.error("❌ [OAuth2Attributes] 지원하지 않는 소셜 로그인: {}", registrationId);
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        log.info("🟡 [OAuth2Attributes] ofKakao 시작");
        log.info("🟡 [OAuth2Attributes] attributes: {}", attributes);

        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            log.info("🟡 [OAuth2Attributes] kakaoAccount: {}", kakaoAccount);

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            log.info("🟡 [OAuth2Attributes] profile: {}", profile);

            String socialId = String.valueOf(attributes.get("id"));
            String nickname = (String) profile.get("nickname");
            String email = (String) kakaoAccount.get("email");

            log.info("🟡 [OAuth2Attributes] 추출된 정보 - socialId: {}, nickname: {}, email: {}", socialId, nickname, email);

            return OAuth2Attributes.builder()
                    .socialId(socialId)
                    .name(nickname)
                    .email(email)
                    .attributes(attributes)
                    .nameAttributeKey(userNameAttributeName)
                    .loginProvider(LoginProvider.KAKAO)
                    .build();
        } catch (Exception e) {
            log.error("❌ [OAuth2Attributes] ofKakao 실패", e);
            throw e;
        }
    }
}
