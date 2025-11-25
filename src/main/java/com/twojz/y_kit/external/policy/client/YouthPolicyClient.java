package com.twojz.y_kit.external.policy.client;

import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.external.policy.dto.YouthPolicyResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouthPolicyClient {
    private final WebClient policyClient;

    @Value("${POLICY_API_KEY}")
    private String apiKey;

    public Mono<YouthPolicyResponse> fetchPolicies(int pageNum, int pageSize) {
        return policyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apiKeyNm", apiKey)
                        .queryParam("pageNum", pageNum)
                        .queryParam("pageSize", pageSize)
                        .queryParam("rtnType", "json")
                        .build())
                .retrieve()
                .bodyToMono(YouthPolicyResponse.class);
    }

    public Mono<List<YouthPolicy>> fetchAllPolicies() {
        return fetchAllPoliciesRecursive(1, 100, new ArrayList<>());
    }

    private Mono<List<YouthPolicy>> fetchAllPoliciesRecursive(int pageNum, int pageSize, List<YouthPolicy> accumulated) {
        return fetchPolicies(pageNum, pageSize)
                .flatMap(response -> {
                    int total = response.getResult().getPagging().getTotCount();
                    List<YouthPolicy> currentPage = response.getResult().getYouthPolicyList();

                    if (currentPage != null && !currentPage.isEmpty()) {
                        accumulated.addAll(currentPage);
                        log.info("페이지 {} 조회 완료: {} 건 / 전체 {} 건", pageNum, currentPage.size(), total);
                    }

                    if (accumulated.size() < total) {
                        return fetchAllPoliciesRecursive(pageNum + 1, pageSize, accumulated);
                    }

                    log.info("전체 조회 완료: 총 {} 건", accumulated.size());
                    return Mono.just(accumulated);
                });
    }
}
