package com.twojz.y_kit.policy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.external.openai.OpenAIService;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PolicyAiAnalysisService {
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    /**
     * 정책명과 설명을 기반으로 AI 분석을 요청하고, 결과를 비동기적으로 반환합니다.
     * * @param policyName 정책명
     * @param policyDescription 정책 설명 (원본 텍스트)
     * @return Mono<AiAnalysis> AI 분석 결과 객체
     */
    public Mono<AiAnalysis> generateAnalysis(String policyName, String policyDescription) {
        String prompt = buildAiPrompt(policyName, policyDescription);

        return openAIService.getCompletion(prompt)
                .flatMap(this::parseJsonToAiAnalysis);
    }

    private String buildAiPrompt(String policyName, String policyDescription) {
        return String.format(
                "정책명: %s\n" +
                        "정책 설명: %s\n\n" +
                        "### 지시사항 및 출력 형식\n\n" +
                        "답변 전체는 격식 있는 종결체(\"~습니다.\")로 작성하고 JSON 외의 문구는 일절 포함하지 마세요.\n" +
                        "1. 설명 (summary): 위 정책 내용을 누구나 이해할 수 있도록 쉬운 말로 한 문단으로 보충 설명해주세요.\n" +
                        "2. 장점 (advantages): 이 정책의 장점 3가지를 구체적으로 작성해주세요.\n" +
                        "3. 단점 (disadvantages): 이 정책의 단점 3가지를 구체적으로 작성해주세요.\n" +
                        "4. 출력 형식: 최종 결과는 오직 아래 형식의 JSON 객체 1개만 출력해야 합니다.\n" +
                        "출력 형식:\n" +
                        "{\n" +
                        "\"summary\": \"정책을 쉬운 말로 보충 설명한 내용\",\n" +
                        "\"advantages\": [\"장점1\", \"장점2\", \"장점3\"],\n" +
                        "\"disadvantages\": [\"단점1\", \"단점2\", \"단점3\"]\n" +
                        "}",
                policyName, policyDescription
        );
    }

    /**
     * JSON 문자열을 AiAnalysis 객체로 역직렬화하는 내부 로직입니다.
     */
    private Mono<AiAnalysis> parseJsonToAiAnalysis(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return Mono.empty();
        }
        try {
            AiAnalysis analysis = objectMapper.readValue(jsonString, AiAnalysis.class);
            return Mono.just(analysis);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("JSON 역직렬화 오류 ", e));
        }
    }
}
