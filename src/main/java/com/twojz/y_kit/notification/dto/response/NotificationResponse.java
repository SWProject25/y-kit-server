package com.twojz.y_kit.notification.dto.response;

import com.twojz.y_kit.notification.entity.NotificationEntity;
import com.twojz.y_kit.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private Boolean isRead;
    private String deepLink;
    private LocalDateTime createdAt;

    public static NotificationResponse from(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .type(entity.getType())
                .isRead(entity.getIsRead())
                .deepLink(entity.getDeepLink())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}