package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.auth.JwtTokenProvider;
import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final UserDeviceRepository userDeviceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserFindService userFindService;

    @Value("${JWT_REFRESH_TOKEN_VALIDITY}")
    private long refreshTokenValidityInMilliseconds;

    /**
     * 리프레쉬 토큰 생성 및 디바이스에 저장
     * @param userId 사용자 ID
     * @param deviceToken FCM 디바이스 토큰
     * @return 생성된 리프레쉬 토큰
     */
    public String createAndSaveRefreshToken(Long userId, String deviceToken) {
        UserEntity user = userFindService.findUser(userId);

        // 디바이스 찾기
        UserDeviceEntity device = userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "등록된 디바이스를 찾을 수 없습니다. 먼저 디바이스를 등록해주세요."
                ));

        // 리프레쉬 토큰 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 만료 시간 계산 (JWT 설정값 기반)
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenValidityInMilliseconds / 1000);

        // 디바이스에 리프레쉬 토큰 저장
        device.updateRefreshToken(refreshToken, expiresAt);

        log.info("리프레쉬 토큰 생성 및 저장 완료 - userId: {}, deviceId: {}", userId, device.getId());

        return refreshToken;
    }

    /**
     * 리프레쉬 토큰 검증 및 사용자 ID 반환
     * @param refreshToken 리프레쉬 토큰
     * @return 사용자 ID
     */
    @Transactional(readOnly = true)
    public Long validateAndGetUserId(String refreshToken) {
        // 1. JWT 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레쉬 토큰입니다.");
        }

        // 2. DB에서 리프레쉬 토큰으로 디바이스 찾기
        UserDeviceEntity device = userDeviceRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "존재하지 않는 리프레쉬 토큰입니다."
                ));

        // 3. 디바이스 활성 상태 확인
        if (!device.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비활성화된 디바이스입니다.");
        }

        // 4. 토큰 만료 확인
        if (device.isRefreshTokenExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "만료된 리프레쉬 토큰입니다.");
        }

        // 5. JWT에서 추출한 userId와 DB의 userId 일치 확인
        Long jwtUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (!jwtUserId.equals(device.getUser().getId())) {
            log.error("토큰 불일치 감지 - JWT userId: {}, DB userId: {}", jwtUserId, device.getUser().getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 정보가 일치하지 않습니다.");
        }

        return device.getUser().getId();
    }

    /**
     * 리프레쉬 토큰 무효화 (로그아웃)
     * @param refreshToken 리프레쉬 토큰
     */
    public void invalidateRefreshToken(String refreshToken) {
        userDeviceRepository.findByRefreshToken(refreshToken)
                .ifPresent(device -> {
                    device.deactivate();
                    log.info("리프레쉬 토큰 무효화 완료 - deviceId: {}", device.getId());
                });
    }

    /**
     * 특정 디바이스의 리프레쉬 토큰 무효화
     * @param userId 사용자 ID
     * @param deviceToken FCM 디바이스 토큰
     */
    public void invalidateDeviceRefreshToken(Long userId, String deviceToken) {
        userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .ifPresent(device -> {
                    device.deactivate();
                    log.info("디바이스 리프레쉬 토큰 무효화 완료 - userId: {}, deviceId: {}", userId, device.getId());
                });
    }
}
