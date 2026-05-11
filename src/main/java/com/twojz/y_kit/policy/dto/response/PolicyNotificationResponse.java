package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
import com.twojz.y_kit.policy.repository.PolicyNotificationQueryRepository.PolicyNotificationSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "정책 알림 신청 응답")
public class PolicyNotificationResponse {
    @Schema(description = "정책 ID")
    private Long policyId;

    @Schema(description = "정책명")
    private String policyName;

    @Schema(description = "정책 요약")
    private String summary;

    @Schema(description = "마감일")
    private LocalDate applicationDeadlineDate;

    @Schema(description = "알림 신청일")
    private LocalDateTime createdAt;

    @Schema(description = "알림 발송 여부")
    private boolean notificationSent;

    public static PolicyNotificationResponse from(PolicyNotificationSummary summary) {
        return PolicyNotificationResponse.builder()
                .policyId(summary.policyId())
                .policyName(summary.policyName())
                .summary(summary.summary())
                .applicationDeadlineDate(summary.applicationDeadlineDate())
                .createdAt(summary.createdAt())
                .notificationSent(summary.notificationSent())
                .build();
    }
}
