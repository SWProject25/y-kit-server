package com.twojz.y_kit.notification.controller;

import com.twojz.y_kit.notification.dto.response.NotificationListResponse;
import com.twojz.y_kit.notification.dto.response.NotificationResponse;
import com.twojz.y_kit.notification.entity.NotificationEntity;
import com.twojz.y_kit.notification.repository.NotificationRepository;
import com.twojz.y_kit.notification.service.NotificationCommandService;
import com.twojz.y_kit.notification.service.NotificationFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "알림 API")
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@RestController
public class NotificationController {
    private final NotificationFindService notificationFindService;
    private final NotificationCommandService notificationCommandService;

    /**
     * 내 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(Authentication authentication) {
        Long userId = extractUserId(authentication);

        List<NotificationEntity> notifications = notificationFindService.findByUser(userId);
        long unreadCount = notificationFindService.countUnread(userId);

        List<NotificationResponse> responseList = notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                NotificationListResponse.builder()
                        .notifications(responseList)
                        .unreadCount(unreadCount)
                        .build()
        );
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(notificationFindService.countUnread(userId));
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {

        Long userId = extractUserId(authentication);

        NotificationEntity notification =
                notificationFindService.findByIdAndUser(notificationId, userId);

        if (notification == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        notificationCommandService.markAsRead(notification);

        return ResponseEntity.ok().build();
    }

    /**
     * 전체 읽음 처리
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = extractUserId(authentication);
        notificationCommandService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable Long notificationId) {

        Long userId = extractUserId(authentication);

        NotificationEntity notification =
                notificationFindService.findByIdAndUser(notificationId, userId);

        if (notification == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        notificationCommandService.delete(notification);

        return ResponseEntity.noContent().build();
    }

    /**
     * 전체 알림 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(Authentication authentication) {
        Long userId = extractUserId(authentication);

        notificationCommandService.deleteAllByUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}