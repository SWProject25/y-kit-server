package com.twojz.y_kit.policy.scheduler;

import com.twojz.y_kit.policy.service.PolicySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicySyncScheduler {
    private final PolicySyncService policySyncService;

    /**
     * 매일 밤 12시에 정책 동기화 실행
     * cron: 초 분 시 일 월 요일
     * "0 0 0 * * *" = 매일 자정 (00:00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void syncPoliciesDaily() {
        log.info("========================================");
        log.info("일일 정책 동기화 스케줄러 시작");
        log.info("========================================");

        try {
            policySyncService.syncAllPolicies();
            log.info("일일 정책 동기화 스케줄러 완료");
        } catch (Exception e) {
            log.error("일일 정책 동기화 중 오류 발생", e);
        }
    }
}
