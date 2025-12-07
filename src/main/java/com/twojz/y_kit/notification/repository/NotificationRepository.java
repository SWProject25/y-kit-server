package com.twojz.y_kit.notification.repository;

import com.twojz.y_kit.notification.entity.NotificationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 사용자 알림 목록 최신순
    List<NotificationEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 읽지 않은 알림 개수
    long countByUserIdAndIsReadFalse(Long userId);

    // 전체 읽음 처리
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    // 30일 이상 오래된 알림 삭제(배치용)
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.createdAt < :cutoffDate")
    void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 특정 알림 조회 (Optional 반환)
    @Query("SELECT n FROM NotificationEntity n WHERE n.id = :notificationId AND n.user.id = :userId")
    Optional<NotificationEntity> findByIdAndUserId(@Param("notificationId") Long notificationId,
                                                   @Param("userId") Long userId);

    // 사용자의 모든 알림 삭제
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}