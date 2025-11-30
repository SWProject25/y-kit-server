package com.twojz.y_kit.notification.service;

import com.twojz.y_kit.notification.entity.NotificationEntity;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFindService {
    private final NotificationRepository notificationRepository;

    /**
     * 사용자 알림 목록 조회 (최신순)
     */
    public List<NotificationEntity> findByUser(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 읽지 않은 알림 개수
     */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 사용자 소유 알림 조회 (못 찾으면 예외 발생)
     */
    public NotificationEntity findByIdAndUser(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
    }
}
