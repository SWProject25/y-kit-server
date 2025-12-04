package com.twojz.y_kit.policy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.external.openai.OpenAIService;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.vo.AiAnalysis;
import com.twojz.y_kit.policy.repository.PolicyDetailRepository;
import com.twojz.y_kit.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyAiAnalysisService {
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;
    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;

    /**
     * 정책명과 설명을 기반으로 AI 분석을 요청하고, 결과를 비동기적으로 반환합니다.
     * @param policyName 정책명
     * @param policyDescription 정책 설명 (원본 텍스트)
     * @return Mono<AiAnalysis> AI 분석 결과 객체
     */
    public Mono<AiAnalysis> generateAnalysis(String policyName, String policyDescription) {
        String prompt = buildAiPrompt(policyName, policyDescription);

        return openAIService.getCompletion(prompt)
                .flatMap(this::parseJsonToAiAnalysis);
    }

    /**
     * AI 분석이 없는 정책 개수 조회
     */
    public long countPoliciesWithoutAi() {
        return policyRepository.countByAiAnalysisIsNull();
    }

    /**
     * AI 분석이 없는 정책 페이징 조회
     */
    public Page<PolicyEntity> findPoliciesWithoutAi(int pageNum, int pageSize) {
        return policyRepository.findAllByAiAnalysisIsNull(
                PageRequest.of(pageNum, pageSize)
        );
    }

    /**
     * 한 정책의 AI 분석을 처리하고 저장합니다.
     * @param policy 처리할 정책 엔티티
     */
    @Transactional
    public void processAiAnalysis(PolicyEntity policy) {
        PolicyDetailEntity detail = policyDetailRepository.findByPolicy(policy)
                .orElseThrow(() -> new RuntimeException("정책 상세 정보 없음"));

        String name = detail.getPlcyNm();
        String desc = detail.getPlcyExplnCn();

        // 필수 정보 검증
        if (name == null || name.trim().isEmpty() ||
                desc == null || desc.trim().isEmpty() || desc.length() < 10) {
            log.warn("AI 분석 스킵 (필수 정보 부족) - policyNo: {}", policy.getPolicyNo());
            return;
        }

        // AI 분석 호출
        AiAnalysis analysis = generateAnalysis(name, desc).block();

        if (analysis != null) {
            policy.updateAiAnalysis(analysis);
            policyRepository.save(policy);
            log.debug("AI 분석 저장 성공 - policyNo: {}", policy.getPolicyNo());
        } else {
            throw new RuntimeException("AI 분석 결과가 null입니다");
        }
    }

    private String buildAiPrompt(String policyName, String policyDescription) {
        return String.format(
                """
                        정책명: %s
                        정책 설명: %s

                        ### 지시사항 및 출력 형식
                        답변 전체는 격식 있는 종결체("~습니다.")로 작성하고 JSON 외의 문구는 일절 포함하지 마세요.
                        1. 설명 (summary): 위 정책 내용을 누구나 이해할 수 있도록 쉬운 말로 한 문단으로 보충 설명해주세요.
                        2. 장점 (advantages): 이 정책의 장점 3가지를 구체적으로 작성해주세요.
                        3. 단점 (disadvantages): 이 정책의 단점 3가지를 구체적으로 작성해주세요.
                        4. 출력 형식: 최종 결과는 오직 아래 형식의 JSON 객체 1개만 출력해야 합니다.
                        출력 형식:
                        {
                        "summary": "정책을 쉬운 말로 보충 설명한 내용",
                        "advantages": ["장점1", "장점2", "장점3"],
                        "disadvantages": ["단점1", "단점2", "단점3"]
                        }""",
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