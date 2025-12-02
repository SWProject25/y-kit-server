package com.twojz.y_kit.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.twojz.y_kit.notification.entity.NotificationEntity;
import com.twojz.y_kit.notification.entity.NotificationType;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserDeviceService userDeviceService;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * 통합 호출 메서드 (외부에서 호출)
     */
    public void sendNotification(UserEntity user, String title, String body, NotificationType type, String deepLink) {
        try {
            NotificationEntity notification = saveNotification(user, title, body, type, deepLink);
            sendFCMNotification(user, notification);
        } catch (Exception e) {
            log.error("알림 전송 실패 - userId: {}", user.getId(), e);
        }
    }

    /**
     * DB에 알림 저장
     */
    @Transactional
    public NotificationEntity saveNotification(UserEntity user, String title, String body, NotificationType type, String deepLink) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .deepLink(deepLink)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * FCM 알림 전송
     */
    public void sendFCMNotification(UserEntity user, NotificationEntity notification) {
        List<String> deviceTokens = userDeviceService.getNotificationEnabledTokens(user.getId());

        if (deviceTokens.isEmpty()) return;

        for (String token : deviceTokens) {
            try {
                Message message = buildFCMMessage(token, notification);
                String response = firebaseMessaging.send(message);
                log.info("FCM 전송 성공 - userId: {}, messageId: {}", user.getId(), response);
            } catch (Exception e) {
                log.error("FCM 전송 실패 - userId: {}, token: {}", user.getId(), token.substring(0, 10) + "...", e);
            }
        }
    }

    /**
     * Message 빌드 분리
     */
    private Message buildFCMMessage(String token, NotificationEntity notification) {
        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .putData("type", notification.getType().name())
                .putData("notificationId", notification.getId().toString())
                .putData("deepLink", notification.getDeepLink() != null ? notification.getDeepLink() : "")
                .build();
    }
}