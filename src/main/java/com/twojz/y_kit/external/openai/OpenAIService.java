package com.twojz.y_kit.external.openai;

import com.twojz.y_kit.external.openai.dto.response.OpenAIResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {
    private final WebClient openAIClient;

    @Value("${openai.api.model}")
    private String model;

    public Mono<String> getCompletion(String prompt) {
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
                .doOnError(e -> System.err.println("OpenAI API 호출 에러: " + e.getMessage()));
    }
}
