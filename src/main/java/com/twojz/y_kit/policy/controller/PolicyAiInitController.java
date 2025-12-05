package com.twojz.y_kit.policy.controller;

import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.service.PolicyAiAnalysisService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/policy-ai")
@RequiredArgsConstructor
@Slf4j
public class PolicyAiInitController {
    private final PolicyAiAnalysisService aiAnalysisService;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger processed = new AtomicInteger(0);
    private final AtomicInteger success = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);
    private volatile String lastError = null;

    @GetMapping("/init/start")
    public ResponseEntity<?> startInit(
            @RequestParam(defaultValue = "0") long delayMs,
            @RequestParam(defaultValue = "5") int parallelCount) {

        if (!isRunning.compareAndSet(false, true)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "이미 실행 중입니다. /init/status 확인하세요"));
        }

        long remaining = aiAnalysisService.countPoliciesWithoutAi();
        if (remaining == 0) {
            isRunning.set(false);
            return ResponseEntity.ok()
                    .body(Map.of("message", "모든 정책의 AI 분석이 완료되었습니다"));
        }

        processed.set(0);
        success.set(0);
        failed.set(0);
        lastError = null;

        new Thread(() -> {
            try {
                runAiAnalysisInit(delayMs, parallelCount);
            } catch (Exception e) {
                log.error("AI 분석 초기화 중 예외 발생", e);
                lastError = e.getMessage();
            } finally {
                isRunning.set(false);
            }
        }, "ai-analysis-init").start();

        return ResponseEntity.ok(Map.of(
                "message", "AI 분석 시작됨",
                "remaining", remaining,
                "delayMs", delayMs,
                "parallelCount", parallelCount,
                "estimatedHours", String.format("%.1f", (remaining * delayMs) / 3600000.0 / parallelCount),
                "statusUrl", "/admin/policy-ai/init/status"
        ));
    }

    @GetMapping("/init/status")
    public ResponseEntity<?> getStatus() {
        long remaining = aiAnalysisService.countPoliciesWithoutAi();
        long completed = aiAnalysisService.countPoliciesWithAi();
        long total = completed + remaining;

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("running", isRunning.get());
        status.put("processedThisSession", processed.get());
        status.put("successThisSession", success.get());
        status.put("failedThisSession", failed.get());
        status.put("totalCompleted", completed);
        status.put("remaining", remaining);
        status.put("total", total);
        status.put("progress", total > 0 ?
                String.format("%.1f%%", (completed * 100.0 / total)) : "100%");

        if (lastError != null) {
            status.put("lastError", lastError);
        }

        if (!isRunning.get() && remaining > 0) {
            status.put("message", "중단됨 또는 에러 발생. 다시 시작하려면 /init/start 호출");
        } else if (!isRunning.get() && remaining == 0) {
            status.put("message", "✅ 모든 정책의 AI 분석 완료!");
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/init/stop")
    public ResponseEntity<?> stopInit() {
        if (!isRunning.get()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "실행 중인 작업이 없습니다"));
        }

        isRunning.set(false);

        return ResponseEntity.ok(Map.of(
                "message", "중단 요청됨. 현재 처리 중인 정책 완료 후 중단됩니다",
                "processed", processed.get(),
                "remaining", aiAnalysisService.countPoliciesWithoutAi()
        ));
    }

    private void runAiAnalysisInit(long delayMs, int parallelCount) {
        log.info("=== AI 분석 초기화 시작 ===");
        log.info("병렬 처리 수: {}, 딜레이: {}ms", parallelCount, delayMs);

        long startTime = System.currentTimeMillis();
        int pageSize = 100;
        AtomicInteger consecutiveFailures = new AtomicInteger(0);
        final int MAX_CONSECUTIVE_FAILURES = 20;

        ExecutorService executor = Executors.newFixedThreadPool(parallelCount);

        try {
            while (isRunning.get()) {
                Page<PolicyEntity> page = aiAnalysisService
                        .findPoliciesWithoutAi(0, pageSize);

                if (page.isEmpty()) {
                    log.info("✅ 모든 정책 처리 완료!");
                    break;
                }

                log.info("=== 배치 처리 시작 ({}개 정책, {}개 병렬) ===",
                        page.getContent().size(), parallelCount);

                List<PolicyEntity> policies = page.getContent();
                CountDownLatch latch = new CountDownLatch(policies.size());

                for (PolicyEntity policy : policies) {
                    if (!isRunning.get()) {
                        log.warn("중단 요청으로 인한 종료");
                        executor.shutdownNow();
                        return;
                    }

                    executor.submit(() -> {
                        try {
                            aiAnalysisService.processAiAnalysis(policy);
                            success.incrementAndGet();
                            consecutiveFailures.set(0);

                        } catch (Exception e) {
                            failed.incrementAndGet();
                            int failures = consecutiveFailures.incrementAndGet();
                            lastError = e.getMessage();

                            log.error("실패 (연속 {}회) - policyNo: {}, error: {}",
                                    failures, policy.getPolicyNo(), e.getMessage());

                            if (failures >= MAX_CONSECUTIVE_FAILURES) {
                                log.error("!!! 연속 {}회 실패. OpenAI 크레딧 또는 Rate Limit 확인 필요 !!!",
                                        failures);
                                log.error("잠시 후 다시 /init/start 호출하면 이어서 진행됩니다");
                                isRunning.set(false);
                            }
                        } finally {
                            latch.countDown();

                            int currentProcessed = processed.incrementAndGet();

                            if (currentProcessed % 10 == 0) {
                                long elapsed = System.currentTimeMillis() - startTime;
                                long remaining = aiAnalysisService.countPoliciesWithoutAi();
                                long estimatedRemaining = remaining > 0 ?
                                        (elapsed * remaining / currentProcessed) : 0;

                                log.info("진행: {} (성공: {}, 실패: {}) | 남은: {}개 | 예상 남은 시간: {}",
                                        currentProcessed, success.get(), failed.get(), remaining,
                                        formatDuration(estimatedRemaining));
                            }
                        }
                    });

                    if (delayMs > 0) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(delayMs / parallelCount);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("인터럽트로 인한 중단");
                            executor.shutdownNow();
                            return;
                        }
                    }
                }

                // 현재 배치 완료 대기
                try {
                    latch.await(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("배치 대기 중 인터럽트");
                    executor.shutdownNow();
                    return;
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("=== AI 분석 초기화 완료 ===");
        log.info("총 처리: {}, 성공: {}, 실패: {}",
                processed.get(), success.get(), failed.get());
        log.info("소요 시간: {}", formatDuration(totalTime));
    }

    private String formatDuration(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;

        if (hours > 0) {
            return String.format("%d시간 %d분", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
}