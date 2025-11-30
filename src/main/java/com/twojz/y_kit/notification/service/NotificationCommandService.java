package com.twojz.y_kit.notification.service;

import com.twojz.y_kit.notification.entity.NotificationEntity;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {
    private final NotificationRepository notificationRepository;

    public void markAsRead(NotificationEntity notification) {
        notification.markAsRead();
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    public void delete(NotificationEntity notification) {
        notificationRepository.delete(notification);
    }

    public void deleteAllByUser(Long userId) {
        List<NotificationEntity> notifications =
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        notificationRepository.deleteAll(notifications);
    }
}
