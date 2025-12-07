package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.dto.response.UserDeviceResponse;
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
    private static final int MAX_DEVICES_PER_USER = 3;

    private final UserDeviceRepository userDeviceRepository;
    private final UserFindService userFindService;

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
     * 내 디바이스 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserDeviceResponse> getMyDevices(Long userId) {
        UserEntity user = userFindService.findUser(userId);
        return userDeviceRepository.findByUserOrderByLastLoginDesc(user)
                .stream()
                .map(UserDeviceResponse::from)
                .toList();
    }

    /**
     * 특정 디바이스 삭제 (강제 로그아웃)
     */
    public void removeDevice(Long userId, Long deviceId) {
        UserDeviceEntity device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다."));

        // 본인의 디바이스인지 확인
        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 디바이스만 삭제할 수 있습니다.");
        }

        userDeviceRepository.delete(device);
        log.info("✅ 디바이스 삭제 완료 - userId: {}, deviceId: {}, deviceName: {}",
                userId, deviceId, device.getDeviceName());
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
     * 다른 사용자의 동일 토큰 삭제 (디바이스 재사용 시)
     */
    private void deactivateOtherUserDevice(Long currentUserId, String deviceToken) {
        userDeviceRepository.findFirstByDeviceToken(deviceToken)
                .filter(device -> !device.getUser().getId().equals(currentUserId))
                .ifPresent(device -> {
                    Long oldUserId = device.getUser().getId();
                    userDeviceRepository.delete(device); // 비활성화 대신 삭제
                    userDeviceRepository.flush(); // 즉시 DB 반영
                    log.warn("⚠️ 다른 사용자 디바이스 삭제 - oldUserId: {}, newUserId: {}",
                            oldUserId, currentUserId);
                });
    }

    /**
     * 새 디바이스 등록
     */
    private void registerNewDevice(Long userId, String deviceName, String deviceToken) {
        try {
            UserEntity user = userFindService.findUser(userId);

            enforceDeviceLimit(user);

            UserDeviceEntity newDevice = UserDeviceEntity.builder()
                    .user(user)
                    .deviceName(deviceName)
                    .deviceToken(deviceToken)
                    .isActive(true)
                    .notificationEnabled(true)
                    .lastLogin(LocalDateTime.now())
                    .build();

            userDeviceRepository.save(newDevice);
            log.info("✅ 새 디바이스 등록 완료 - userId: {}, deviceName: {}", userId, deviceName);

        } catch (DataIntegrityViolationException e) {
            log.warn("⚠️ 디바이스 등록 중 중복 에러 발생 - userId: {}, 동시성 처리 시작", userId);
            handleDuplicateToken(userId, deviceName, deviceToken, e);
        }
    }

    /**
     * 기기 개수 제한 적용 (최대 3개)
     * 초과 시 가장 오래 로그인 안 한 기기 강제 로그아웃
     */
    private void enforceDeviceLimit(UserEntity user) {
        long activeDeviceCount = userDeviceRepository.countByUserAndIsActiveTrue(user);

        if (activeDeviceCount >= MAX_DEVICES_PER_USER) {
            // 가장 오래 로그인 안 한 기기 찾아서 비활성화
            userDeviceRepository.findFirstByUserAndIsActiveTrueOrderByLastLoginAsc(user)
                    .ifPresent(oldestDevice -> {
                        oldestDevice.deactivate();
                        userDeviceRepository.save(oldestDevice); // 명시적으로 저장
                        userDeviceRepository.flush(); // 즉시 DB 반영
                        log.warn("기기 제한 초과로 강제 로그아웃 - userId: {}, 제거된 기기: {}, 마지막 로그인: {}",
                                user.getId(), oldestDevice.getDeviceName(), oldestDevice.getLastLogin());
                    });
        }
    }

    /**
     * 중복 토큰 예외 처리 (동시성 이슈)
     */
    private void handleDuplicateToken(Long userId, String deviceName, String deviceToken,
                                      DataIntegrityViolationException e) {
        log.warn("⚠️ 중복 토큰 예외 발생 - userId: {}, 재시도 중...", userId);

        UserDeviceEntity existingDevice = userDeviceRepository.findFirstByDeviceToken(deviceToken)
                .orElseThrow(() -> new IllegalStateException("디바이스 등록 실패", e));

        if (!existingDevice.getUser().getId().equals(userId)) {
            // 다른 사용자의 디바이스면 삭제하고 새로 생성
            log.warn("⚠️ 다른 사용자 디바이스 발견 - oldUserId: {}, newUserId: {}, 삭제 후 재등록",
                    existingDevice.getUser().getId(), userId);
            Long oldUserId = existingDevice.getUser().getId();

            userDeviceRepository.delete(existingDevice);
            userDeviceRepository.flush();

            // 새 디바이스 생성 (재귀 호출 방지)
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
            log.info("✅ 디바이스 재등록 완료 - oldUserId: {}, newUserId: {}", oldUserId, userId);
            return;
        }

        // 같은 사용자면 업데이트
        existingDevice.updateLoginInfo(deviceName, deviceToken);
        log.info("✅ 기존 디바이스 업데이트 완료 - userId: {}", userId);
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