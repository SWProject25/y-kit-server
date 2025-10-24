package com.twojz.y_kit.user.auth;

import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

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
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .socialId(String.valueOf(attributes.get("id")))
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .loginProvider(LoginProvider.KAKAO)
                .build();
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .email(email)
                .name(name)
                .password(null)
                .socialId(socialId)
                .loginProvider(loginProvider)
                .role(Role.USER)
                .build();
    }
}
