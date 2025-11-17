package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.UserDeviceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, Long> {
    /**
     * 디바이스 토큰으로 조회
     */
    Optional<UserDeviceEntity> findByDeviceToken(String deviceToken);

    /**
     * 사용자의 모든 디바이스 조회
     */
    @Query("SELECT d FROM UserDeviceEntity d " +
            "WHERE d.user.id = :userId " +
            "ORDER BY d.lastLogin DESC")
    List<UserDeviceEntity> findAllByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 활성 디바이스만 조회
     */
    @Query("SELECT d FROM UserDeviceEntity d " +
            "WHERE d.user.id = :userId " +
            "AND d.isActive = true " +
            "ORDER BY d.lastLogin DESC")
    List<UserDeviceEntity> findActiveDevicesByUserId(@Param("userId") Long userId);

    /**
     * 활성 디바이스 토큰 목록 조회
     */
    @Query("SELECT d.deviceToken FROM UserDeviceEntity d " +
            "WHERE d.user.id = :userId " +
            "AND d.isActive = true")
    List<String> findActiveDeviceTokensByUserId(@Param("userId") Long userId);

    /**
     * 여러 사용자의 활성 디바이스 토큰 조회
     */
    @Query("SELECT d.deviceToken FROM UserDeviceEntity d " +
            "WHERE d.user.id IN :userIds " +
            "AND d.isActive = true")
    List<String> findActiveDeviceTokensByUserIds(@Param("userIds") List<Long> userIds);
}
