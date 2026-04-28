package com.twojz.y_kit.policy.repository;

import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.user.entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PolicyNotificationQueryRepository {
    List<PolicyNotificationSummary> findNotificationSummariesByUser(UserEntity user);

    List<PendingPolicyNotification> findPendingNotificationsByDeadline(LocalDate targetDate);

    record PolicyNotificationSummary(
            Long policyId,
            String policyName,
            String summary,
            LocalDate applicationDeadlineDate,
            LocalDateTime createdAt,
            boolean notificationSent
    ) {}

    record PendingPolicyNotification(
            PolicyNotificationEntity notification,
            String policyName
    ) {}
}
