package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final UserFindService userFindService;

    /**
     * 디바이스 토큰 등록/업데이트 (로그인 시)
     */
    public void registerOrUpdateDevice(Long userId, String deviceName, String deviceToken) {
        UserDeviceEntity device = userDeviceRepository.findByDeviceToken(deviceToken)
                .orElseGet(() -> {
                    UserEntity user = userFindService.findUser(userId);
                    return userDeviceRepository.save(UserDeviceEntity.builder()
                            .user(user)
                            .deviceName(deviceName)
                            .deviceToken(deviceToken)
                            .isActive(true)
                            .lastLogin(LocalDateTime.now())
                            .build());
                });

        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 디바이스 토큰입니다.");
        }
        device.updateLoginInfo(deviceName);
    }

    /**
     * 디바이스 비활성화 (로그아웃 시)
     */
    public void deactivateDevice(Long userId, String deviceToken) {
        UserDeviceEntity device = userDeviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 디바이스 토큰입니다."));

        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 디바이스 토큰입니다.");
        }

        device.deactivate();
    }

    /**
     * 사용자의 활성 디바이스 토큰 목록
     */
    @Transactional(readOnly = true)
    public List<String> getActiveTokens(Long userId) {
        return userDeviceRepository.findActiveDeviceTokensByUserId(userId);
    }
}