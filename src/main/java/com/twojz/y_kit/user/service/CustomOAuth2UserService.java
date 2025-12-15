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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();


        OAuth2Attributes attributes = OAuth2Attributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2UserAttributes
        );

        UserEntity user = userService.saveSocialUser(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }
}
