package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import java.util.Optional;
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
     * - 같은 사용자의 같은 토큰이면 업데이트
     * - 다른 사용자가 같은 토큰을 가지고 있으면 기존 것은 비활성화하고 새로 등록
     */
    public void registerOrUpdateDevice(Long userId, String deviceName, String deviceToken) {
        UserEntity user = userFindService.findUser(userId);

        Optional<UserDeviceEntity> currentUserDevice =
                userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (currentUserDevice.isPresent()) {
            currentUserDevice.get().updateLoginInfo(deviceName, deviceToken);
            return;
        }

        Optional<UserDeviceEntity> otherUserDevice =
                userDeviceRepository.findFirstByDeviceToken(deviceToken);

        if (otherUserDevice.isPresent() && !otherUserDevice.get().getUser().getId().equals(userId)) {
            otherUserDevice.get().deactivate();
        }

        userDeviceRepository.save(UserDeviceEntity.builder()
                .user(user)
                .deviceName(deviceName)
                .deviceToken(deviceToken)
                .isActive(true)
                .notificationEnabled(true)
                .lastLogin(LocalDateTime.now())
                .build());
    }

    /**
     * 디바이스 비활성화 (로그아웃 시)
     */
    public void deactivateDevice(Long userId, String deviceToken) {
        Optional<UserDeviceEntity> device =
                userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (device.isEmpty()) {
            return;
        }

        device.get().deactivate();
    }

    /**
     * 알림 허용된 활성 디바이스 토큰 목록
     */
    @Transactional(readOnly = true)
    public List<String> getNotificationEnabledTokens(Long userId) {
        return userDeviceRepository.findNotificationEnabledTokensByUserId(userId);
    }

    /**
     * 알림 켜기
     */
    public void enableNotification(Long userId, String deviceToken) {
        Optional<UserDeviceEntity> device =
                userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (device.isEmpty()) {
            return;
        }

        device.get().enableNotification();
    }

    /**
     * 알림 끄기
     */
    public void disableNotification(Long userId, String deviceToken) {
        Optional<UserDeviceEntity> device =
                userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);

        if (device.isEmpty()) {
            return;
        }

        device.get().disableNotification();
    }

    /**
     * 모든 디바이스 알림 켜기
     */
    public void enableAllNotifications(Long userId) {
        List<UserDeviceEntity> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userId);
        devices.forEach(UserDeviceEntity::enableNotification);
    }

    /**
     * 모든 디바이스 알림 끄기
     */
    public void disableAllNotifications(Long userId) {
        List<UserDeviceEntity> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userId);
        devices.forEach(UserDeviceEntity::disableNotification);
    }

    /**
     * 토큰 마스킹 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}
