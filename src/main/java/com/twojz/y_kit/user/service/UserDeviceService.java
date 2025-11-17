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
     */
    public void registerOrUpdateDevice(Long userId, String deviceName, String deviceToken) {
        try {
            Optional<UserDeviceEntity> existingDevice = userDeviceRepository.findByDeviceToken(deviceToken);

            if (existingDevice.isPresent()) {
                // 기존 디바이스가 있는 경우
                UserDeviceEntity device = existingDevice.get();

                // 소유권 검증
                if (!device.getUser().getId().equals(userId)) {
                    throw new IllegalArgumentException("다른 사용자의 디바이스 토큰입니다.");
                }

                // 로그인 정보 업데이트
                device.updateLoginInfo(deviceName, deviceToken);

            } else {
                // 새로운 디바이스 등록
                UserEntity user = userFindService.findUser(userId);
                userDeviceRepository.save(UserDeviceEntity.builder()
                        .user(user)
                        .deviceName(deviceName)
                        .deviceToken(deviceToken)
                        .isActive(true)
                        .lastLogin(LocalDateTime.now())
                        .build());
            }

        } catch (DataIntegrityViolationException e) {
            // 경쟁 조건: 동시에 INSERT 시도 → 재조회 후 업데이트
            log.warn("경쟁 조건 감지 - 재시도. userId: {}, token: {}", userId, maskToken(deviceToken));

            UserDeviceEntity device = userDeviceRepository.findByDeviceToken(deviceToken)
                    .orElseThrow(() -> new IllegalStateException("디바이스 등록 실패", e));

            // 소유권 검증
            if (!device.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("다른 사용자의 디바이스 토큰입니다.");
            }

            // 로그인 정보 업데이트
            device.updateLoginInfo(deviceName, deviceToken);
        }
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