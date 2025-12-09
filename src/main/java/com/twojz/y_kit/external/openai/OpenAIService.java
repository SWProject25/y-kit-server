package com.twojz.y_kit.external.openai;

import com.twojz.y_kit.external.openai.dto.response.OpenAIResponse;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {
    private final WebClient openAIClient;

    public Mono<String> getCompletion(String model, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", prompt
        );

        return openAIClient.post()
                .uri("/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .map(OpenAIResponse::getFirstMessageContent)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof java.io.IOException))
                .onErrorResume(e -> {
                    log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
                    return Mono.just("OpenAI API 호출 중 오류가 발생했습니다.");
                });
    }
}
