package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, Long> {
    // 특정 사용자의 특정 디바이스 토큰 조회
    Optional<UserDeviceEntity> findByUserIdAndDeviceToken(Long userId, String deviceToken);

    // 디바이스 토큰으로 조회 (첫 번째만)
    Optional<UserDeviceEntity> findFirstByDeviceToken(String deviceToken);

    // 활성화되고 알림 허용된 디바이스 토큰만 조회
    @Query("SELECT ud.deviceToken FROM UserDeviceEntity ud " +
            "WHERE ud.user.id = :userId AND ud.isActive = true AND ud.notificationEnabled = true")
    List<String> findNotificationEnabledTokensByUserId(Long userId);

    // 활성화된 모든 디바이스 조회
    List<UserDeviceEntity> findByUserIdAndIsActiveTrue(Long userId);

    // 활성 기기 개수
    long countByUserAndIsActiveTrue(UserEntity user);

    // Refresh Token으로 기기 찾기
    Optional<UserDeviceEntity> findByRefreshToken(String refreshToken);

    // 가장 오래 로그인 안 한 기기 (강제 로그아웃용)
    Optional<UserDeviceEntity> findFirstByUserAndIsActiveTrueOrderByLastLoginAsc(UserEntity user);

    // 사용자의 모든 디바이스 조회 (최근 로그인순)
    List<UserDeviceEntity> findByUserOrderByLastLoginDesc(UserEntity user);

    // 사용자의 모든 디바이스 삭제
    @Modifying
    @Query("DELETE FROM UserDeviceEntity d WHERE d.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
