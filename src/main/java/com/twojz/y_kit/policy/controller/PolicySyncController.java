package com.twojz.y_kit.policy.controller;

import com.twojz.y_kit.policy.service.PolicySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/policy-sync")
@RequiredArgsConstructor
public class PolicySyncController {
    private final PolicySyncService policySyncService;

    @GetMapping("/trigger")
    public ResponseEntity<String> triggerSync() {
        log.info("수동 정책 동기화 요청");
        try {
            policySyncService.syncAllPolicies();
            return ResponseEntity.ok("정책 동기화가 시작되었습니다.");
        } catch (Exception e) {
            log.error("정책 동기화 트리거 실패", e);
            return ResponseEntity.internalServerError()
                    .body("정책 동기화 실패: " + e.getMessage());
        }
    }
}
