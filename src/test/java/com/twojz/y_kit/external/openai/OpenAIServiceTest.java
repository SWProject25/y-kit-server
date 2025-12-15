package com.twojz.y_kit.external.openai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class OpenAIServiceTest {
    @Autowired
    private OpenAIService openAIService;

    @Test
    void shouldGetCompletionFromOpenAI() {
        String testPrompt = "안녕하세요";

        Mono<String> resultMono = openAIService.getCompletion("gpt-4o-mini", testPrompt);

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    System.out.println("OpenAI 응답: " + result);
                    return result != null && !result.isEmpty();
                })
                .verifyComplete();
    }
}
