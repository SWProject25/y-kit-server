package com.twojz.y_kit.user.service;

import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.service.RegionFindService;
import com.twojz.y_kit.user.auth.OAuth2Attributes;
import com.twojz.y_kit.user.dto.request.ProfileCompleteRequest;
import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegionFindService regionFindService;
    private final UserFindService userFindService;

    @Transactional
    public UserEntity saveLocalUser(LocalSignUpRequest request) {
        validateEmailNotExists(request.getEmail());

        Region region = null;
        if(request.getRegion() != null) {
            region = regionFindService.findRegionName(request.getRegion());
        }

        return userRepository.save(UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .role(Role.USER)
                .loginProvider(LoginProvider.LOCAL)
                .region(region)
                .build());
    }

    @Transactional
    public UserEntity saveSocialUser(OAuth2Attributes attributes) {
        return userRepository.findBySocialIdAndLoginProvider(
                        attributes.getSocialId(),
                        attributes.getLoginProvider()
                )
                .orElseGet(() -> {
                    validateEmailNotExists(attributes.getEmail());
                    return userRepository.save(attributes.toEntity());
                });
    }

    @Transactional
    public void completeProfile(Long userId, ProfileCompleteRequest request) {
        UserEntity user = userFindService.findUser(userId);

        Region region = null;
        if (request.getRegion() != null) {
            region = regionFindService.findRegionName(request.getRegion());
        }

        user.completeProfile(
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                region
        );
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }
}