package com.twojz.y_kit.policy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twojz.y_kit.external.openai.OpenAIService;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.dto.response.PolicyComparisonResponse;
import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.service.UserFindService;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyComparisonService {
    private final OpenAIService openAIService;
    private final PolicyFindService policyFindService;
    private final UserFindService userFindService;
    private final ObjectMapper objectMapper;

    @Value("${OPEN_API_COMPARE_MODEL}")
    private String model;

    @Transactional(readOnly = true)
    public PolicyComparisonResponse comparePolicies(Long userId, List<Long> policyIds) {
        UserEntity user = userFindService.findUser(userId);
        List<PolicyEntity> policies = policyFindService.getPoliciesByIds(policyIds);

        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("비교할 정책이 없습니다.");
        }
        if (policies.size() > 4) {
            throw new IllegalArgumentException("최대 4개의 정책만 비교할 수 있습니다.");
        }

        String prompt = buildPrompt(user, policies);

        String aiResponse = openAIService.getCompletion(model, prompt).block();
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new RuntimeException("AI 응답이 비어 있습니다.");
        }

        return parseAIResponse(aiResponse);
    }


    private String buildPrompt(UserEntity user, List<PolicyEntity> policies) {

        return """
사용자 정보와 정책 설명을 기반으로 가장 적합한 정책을 JSON으로만 출력하십시오.

## 사용자 정보
%s

## 정책 목록
%s

## 지시사항
1. 사용자 상황에 맞춰 각 정책의 적합도 점수를 0.0~1.0 사이로 계산하십시오.
2. 출력은 반드시 JSON 객체 1개만 포함해야 하며 다른 텍스트, 설명, 마크다운, 코드블록 등은 절대 포함하지 마십시오.
3. reason은 2~3문장으로 작성하십시오.
4. recommended_policy는 적합도가 가장 높은 정책 1개만 포함합니다.
5. alternatives는 나머지 정책을 적합도 순서대로 나열하십시오.

## 출력(JSON만 출력)
{
  "recommended_policy": {
    "name": "",
    "fit_score": 0.0,
    "reason": ""
  },
  "alternatives": [
    {
      "name": "",
      "fit_score": 0.0,
      "reason": ""
    }
  ]
}
""".formatted(
                buildUserContent(user),
                buildPolicyContent(policies)
        );
    }

    private String buildUserContent(UserEntity user) {
        return """
- 나이: %s세
- 지역: %s
- 성별: %s
- 학력: %s
- 전공: %s
- 고용 상태: %s
""".formatted(
                safe(String.valueOf(user.calculateAge())),
                safe(user.getRegion().getName()),
                safe(user.getGender().getName()),
                safe(user.getEducationLevel().getDescription()),
                safe(user.getMajor().getDescription()),
                safe(user.getEmploymentStatus().getDescription())
        );
    }

    private String buildPolicyContent(List<PolicyEntity> policies) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < policies.size(); i++) {
            PolicyEntity p = policies.get(i);
            PolicyDetailEntity d = p.getDetail();
            if (d == null) continue;

            sb.append("  {\n")
                    .append("    \"name\": \"").append(escape(d.getPlcyNm())).append("\",\n")
                    .append("    \"description\": \"").append(escape(d.getPlcyExplnCn())).append("\"\n")
                    .append("  }");

            if (i < policies.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String escape(String v) {
        if (v == null) return "";
        return v
                .replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }

    private PolicyComparisonResponse parseAIResponse(String aiResponse) {
        try {
            String cleaned = aiResponse
                    .replaceAll("(?s)^.*?\\{", "{")
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            log.info("AI cleaned JSON: {}", cleaned);

            return objectMapper.readValue(cleaned, PolicyComparisonResponse.class);
        } catch (Exception e) {
            log.error("AI JSON 파싱 실패. 원본: {}", aiResponse, e);
            throw new RuntimeException("AI 응답을 파싱하지 못했습니다.");
        }
    }
}
