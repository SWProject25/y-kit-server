package com.twojz.y_kit.user.service;

import com.twojz.y_kit.community.repository.CommunityBookmarkRepository;
import com.twojz.y_kit.community.repository.CommunityCommentRepository;
import com.twojz.y_kit.community.repository.CommunityLikeRepository;
import com.twojz.y_kit.community.repository.CommunityRepository;
import com.twojz.y_kit.group.repository.*;
import com.twojz.y_kit.hotdeal.repository.HotDealBookmarkRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealCommentRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealLikeRepository;
import com.twojz.y_kit.hotdeal.repository.HotDealRepository;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import com.twojz.y_kit.policy.repository.PolicyBookmarkRepository;
import com.twojz.y_kit.policy.repository.PolicyNotificationRepository;
import com.twojz.y_kit.region.entity.Region;
import com.twojz.y_kit.region.service.RegionFindService;
import com.twojz.y_kit.user.auth.OAuth2Attributes;
import com.twojz.y_kit.user.dto.request.ProfileCompleteRequest;
import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.repository.UserBadgeRepository;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import com.twojz.y_kit.user.repository.UserRepository;
import java.util.List;
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
        if(request.getRegionCode() != null) {
            region = regionFindService.findRegionCode(request.getRegionCode());
        }

        String finalNickName = request.getNickName() != null
                ? request.getNickName()
                : createUniqueNickname();

        return userRepository.save(UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickName(finalNickName)
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .region(region)
                .employmentStatus(request.getEmploymentStatus())
                .educationLevel(request.getEducationLevel())
                .major(request.getMajor())
                .role(Role.USER)
                .loginProvider(LoginProvider.LOCAL)
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

                    UserEntity user = UserEntity.builder()
                            .email(attributes.getEmail())
                            .name(attributes.getName())
                            .nickName(createUniqueNickname())
                            .password(null)
                            .socialId(attributes.getSocialId())
                            .loginProvider(attributes.getLoginProvider())
                            .role(Role.USER)
                            .build();

                    return userRepository.save(user);
                });
    }

    @Transactional
    public void completeProfile(Long userId, ProfileCompleteRequest request) {
        UserEntity user = userFindService.findUser(userId);

        Region region = null;
        if (request.getRegionCode() != null && !request.getRegionCode().isEmpty()) {
            region = regionFindService.findRegionCode(request.getRegionCode());
        }

        user.completeProfile(
                request.getName(),
                request.getNickName(),
                request.getBirthDate(),
                request.getGender(),
                region,
                request.getEmploymentStatus(),
                request.getEducationLevel(),
                request.getMajor()
        );
    }

    @Transactional
    public void skipProfile(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        user.skipProfile();
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    private String createUniqueNickname() {
        String nickname;
        do {
            nickname = NicknameGenerator.generate();
        } while (userRepository.existsByNickName(nickname));
        return nickname;
    }
}