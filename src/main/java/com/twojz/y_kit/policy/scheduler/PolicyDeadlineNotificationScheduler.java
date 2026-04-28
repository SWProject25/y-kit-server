package com.twojz.y_kit.policy.scheduler;

import com.twojz.y_kit.notification.entity.NotificationType;
import com.twojz.y_kit.notification.service.NotificationService;
import com.twojz.y_kit.policy.repository.PolicyNotificationQueryRepository.PendingPolicyNotification;
import com.twojz.y_kit.policy.service.PolicyNotificationFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyDeadlineNotificationScheduler {
    private final PolicyNotificationFindService policyNotificationFindService;
    private final NotificationService notificationService;

    /**
     * 매일 오전 10시에 마감 일주일 전 정책 알림 발송
     * 크론 표현식: "0 0 10 * * *" = 매일 오전 10시 0분 0초
     */
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void sendPolicyDeadlineNotifications() {
        log.info("🔔 정책 마감 알림 스케줄러 시작...");

        try {
            // 오늘로부터 7일 후 날짜
            LocalDate deadlineDate = LocalDate.now().plusDays(7);
            log.info("📅 마감일 체크: {}", deadlineDate);

            // 마감 7일 전인 정책들 중 알림 미발송된 것 조회
            List<PendingPolicyNotification> pendingNotifications =
                    policyNotificationFindService.findPendingNotificationsByDeadline(deadlineDate);

            if (pendingNotifications.isEmpty()) {
                log.info("ℹ️ 발송할 마감 알림이 없습니다.");
                return;
            }

            log.info("📬 총 {}건의 마감 알림을 발송합니다.", pendingNotifications.size());

            int successCount = 0;
            int failCount = 0;

            for (PendingPolicyNotification pendingNotification : pendingNotifications) {
                var notification = pendingNotification.notification();
                try {
                    String policyName = pendingNotification.policyName();
                    String title = "⏰ 정책 마감 임박 안내";
                    String body = String.format("'%s' 정책이 일주일 후 마감됩니다. 서둘러 신청하세요!", policyName);
                    String deepLink = "/policies/" + notification.getPolicy().getId();

                    // FCM 알림 전송
                    notificationService.sendNotification(
                            notification.getUser(),
                            title,
                            body,
                            NotificationType.POLICY,
                            deepLink
                    );

                    // 발송 완료 표시
                    notification.markAsSent();
                    successCount++;

                    log.info("✅ 마감 알림 발송 성공 - userId: {}, policyId: {}, policyName: '{}'",
                            notification.getUser().getId(),
                            notification.getPolicy().getId(),
                            policyName);

                } catch (Exception e) {
                    failCount++;
                    log.error("❌ 마감 알림 발송 실패 - userId: {}, policyId: {}",
                            notification.getUser().getId(),
                            notification.getPolicy().getId(),
                            e);
                }
            }

            log.info("🎯 정책 마감 알림 발송 완료 - 성공: {}건, 실패: {}건", successCount, failCount);

        } catch (Exception e) {
            log.error("💥 정책 마감 알림 스케줄러 오류 발생", e);
        }
    }
}
