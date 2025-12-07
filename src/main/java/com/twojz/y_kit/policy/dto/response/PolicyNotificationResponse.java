package com.twojz.y_kit.policy.dto.response;

import com.twojz.y_kit.policy.domain.entity.PolicyNotificationEntity;
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

    public static PolicyNotificationResponse from(PolicyNotificationEntity entity) {
        return PolicyNotificationResponse.builder()
                .policyId(entity.getPolicy().getId())
                .policyName(entity.getPolicy().getDetail().getPlcyNm())
                .summary(entity.getPolicy().getDetail().getPlcyExplnCn())
                .applicationDeadlineDate(entity.getPolicy().getApplication().getAplyEndYmd())
                .createdAt(entity.getCreatedAt())
                .notificationSent(entity.isNotificationSent())
                .build();
    }
}
