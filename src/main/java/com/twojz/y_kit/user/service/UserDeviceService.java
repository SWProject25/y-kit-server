package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.debug("📱 디바이스 등록 시작 - userId: {}, token: {}", userId, maskToken(deviceToken));

        // 1. 현재 사용자의 기존 디바이스 확인 및 업데이트
        if (updateExistingDevice(userId, deviceName, deviceToken)) {
            return;
        }

        // 2. 다른 사용자의 동일 토큰 비활성화
        deactivateOtherUserDevice(userId, deviceToken);

        // 3. 새 디바이스 등록
        registerNewDevice(userId, deviceName, deviceToken);
    }

    /**
     * 기존 디바이스 업데이트
     * @return 업데이트 성공 여부
     */
    private boolean updateExistingDevice(Long userId, String deviceName, String deviceToken) {
        return userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .map(device -> {
                    device.updateLoginInfo(deviceName, deviceToken);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 다른 사용자의 동일 토큰 비활성화
     */
    private void deactivateOtherUserDevice(Long currentUserId, String deviceToken) {
        userDeviceRepository.findFirstByDeviceToken(deviceToken)
                .filter(device -> !device.getUser().getId().equals(currentUserId))
                .ifPresent(device -> {
                    device.deactivate();
                    log.warn("⚠️ 다른 사용자 디바이스 비활성화 - oldUserId: {}, newUserId: {}",
                            device.getUser().getId(), currentUserId);
                });
    }

    /**
     * 새 디바이스 등록
     */
    private void registerNewDevice(Long userId, String deviceName, String deviceToken) {
        try {
            UserEntity user = userFindService.findUser(userId);

            UserDeviceEntity newDevice = UserDeviceEntity.builder()
                    .user(user)
                    .deviceName(deviceName)
                    .deviceToken(deviceToken)
                    .isActive(true)
                    .notificationEnabled(true)
                    .lastLogin(LocalDateTime.now())
                    .build();

            userDeviceRepository.save(newDevice);

        } catch (DataIntegrityViolationException e) {
            handleDuplicateToken(userId, deviceName, deviceToken, e);
        }
    }

    /**
     * 중복 토큰 예외 처리 (동시성 이슈)
     */
    private void handleDuplicateToken(Long userId, String deviceName, String deviceToken,
                                      DataIntegrityViolationException e) {
        UserDeviceEntity device = userDeviceRepository.findFirstByDeviceToken(deviceToken)
                .orElseThrow(() -> new IllegalStateException("디바이스 등록 실패", e));

        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 디바이스 토큰입니다.");
        }

        device.updateLoginInfo(deviceName, deviceToken);
    }

    /**
     * 디바이스 비활성화 (로그아웃 시)
     */
    public void deactivateDevice(Long userId, String deviceToken) {
        userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .ifPresentOrElse(
                        UserDeviceEntity::deactivate,
                        () -> log.warn("⚠️ 비활성화할 디바이스 없음 - userId: {}", userId)
                );
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
        updateNotificationSetting(userId, deviceToken, true);
    }

    /**
     * 알림 끄기
     */
    public void disableNotification(Long userId, String deviceToken) {
        updateNotificationSetting(userId, deviceToken, false);
    }

    /**
     * 알림 설정 변경 (공통 로직)
     */
    private void updateNotificationSetting(Long userId, String deviceToken, boolean enabled) {
        userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .ifPresentOrElse(
                        device -> {
                            if (enabled) {
                                device.enableNotification();
                            } else {
                                device.disableNotification();
                            }
                        },
                        () -> log.warn("⚠️ 디바이스 없음 - userId: {}", userId)
                );
    }

    /**
     * 모든 디바이스 알림 켜기
     */
    public void enableAllNotifications(Long userId) {
        updateAllNotifications(userId, true);
    }

    /**
     * 모든 디바이스 알림 끄기
     */
    public void disableAllNotifications(Long userId) {
        updateAllNotifications(userId, false);
    }

    /**
     * 모든 디바이스 알림 설정 변경 (공통 로직)
     */
    private void updateAllNotifications(Long userId, boolean enabled) {
        List<UserDeviceEntity> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userId);

        devices.forEach(device -> {
            if (enabled) {
                device.enableNotification();
            } else {
                device.disableNotification();
            }
        });
    }

    /**
     * 토큰 마스킹 (로깅/보안용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}