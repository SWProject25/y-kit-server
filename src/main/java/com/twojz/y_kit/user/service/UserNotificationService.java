package com.twojz.y_kit.user.service;

import com.twojz.y_kit.notification.entity.NotificationType;
import com.twojz.y_kit.notification.service.NotificationService;
import com.twojz.y_kit.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserNotificationService {
    private final NotificationService notificationService;

    public void sendWelcomeNotification(UserEntity user) {
        notificationService.sendNotification(user,
                "🎉 Y-Kit에 오신 것을 환영합니다!",
                user.getName() + "님, 청년을 위한 맞춤 정보를 확인해보세요.",
                NotificationType.WELCOME,
                null
        );
    }

    public void sendProfileCompleteReminder(UserEntity user) {
        notificationService.sendNotification(user,
                "📝 프로필 완성하고 맞춤 추천 받기",
                "추가 정보를 입력하면 더 정확한 정책을 추천해드려요!",
                NotificationType.PROFILE_COMPLETE_REMINDER,
                "/profile/complete"
        );
    }

    public void sendProfileCompletedNotification(UserEntity user) {
        notificationService.sendNotification(user,
                "프로필 완성!",
                "이제 " + user.getName() + "님을 위한 맞춤 정책을 추천받을 수 있어요.",
                NotificationType.USER,
                "/policies/recommended"
        );
    }
}
