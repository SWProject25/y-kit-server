package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.auth.OAuth2Attributes;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("🔵 [OAuth2] loadUser 시작 - registrationId: {}", userRequest.getClientRegistration().getRegistrationId());

        try {
            Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
            log.info("🔵 [OAuth2] 카카오로부터 사용자 정보 받음: {}", oAuth2UserAttributes);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("🔵 [OAuth2] registrationId: {}", registrationId);

            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUserNameAttributeName();
            log.info("🔵 [OAuth2] userNameAttributeName: {}", userNameAttributeName);

            OAuth2Attributes attributes = OAuth2Attributes.of(
                    registrationId,
                    userNameAttributeName,
                    oAuth2UserAttributes
            );
            log.info("🔵 [OAuth2] OAuth2Attributes 생성 완료 - email: {}, name: {}", attributes.getEmail(), attributes.getName());

            UserEntity user = userService.saveSocialUser(attributes);
            log.info("🔵 [OAuth2] 사용자 저장/조회 완료 - userId: {}, email: {}", user.getId(), user.getEmail());

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName())),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        } catch (Exception e) {
            log.error("❌ [OAuth2] loadUser 실패", e);
            throw e;
        }
    }
}
