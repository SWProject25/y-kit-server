package com.twojz.y_kit.user.scheduler;

import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import com.twojz.y_kit.user.service.UserNotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileReminderScheduler {
    private final UserFindService userFindService;
    private final UserNotificationService userNotificationService;

    /**
     * 매일 오전 10시에 프로필 미완성 사용자에게 리마인더 발송
     * 가입 후 7일 이내 사용자만 대상
     */
    @Scheduled(cron = "0 0 10 * * *") // 매일 오전 10시
    @Transactional(readOnly = true)
    public void sendProfileReminders() {
        List<UserEntity> targetUsers = userFindService.findUsersForProfileReminder(LocalDateTime.now().minusDays(7));
        for (UserEntity user : targetUsers) {
            try {
                userNotificationService.sendProfileCompleteReminder(user);
            } catch (Exception e) {
                log.error("프로필 리마인더 발송 실패 - userId: {}", user.getId(), e);
            }
        }
    }
}
