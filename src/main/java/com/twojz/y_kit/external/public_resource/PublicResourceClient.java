package com.twojz.y_kit.external.public_resource;

import com.twojz.y_kit.external.public_resource.dto.PublicResourceResponse;
import com.twojz.y_kit.facillty.domain.entity.FacilityCategory;
import com.twojz.y_kit.facillty.domain.entity.FacilityEntity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicResourceClient {
    public static final int PAGE_SIZE = 100;
    private final WebClient publicWebResourceClient;

    @Value("${PUBLIC_RESOURCE_API_KEY}")
    private String apiKey;

    /**
     * 카테고리 + pageNo 기반 API 호출
     */
    public Mono<PublicResourceResponse> fetchCategoryPage(String categoryCode, int pageNo) {
        Map<String, Object> body = new HashMap<>();
        body.put("pageNo", pageNo);
        body.put("numOfRows", PAGE_SIZE);

        if (categoryCode != null && !categoryCode.isEmpty()) {
            body.put("rsrcClsCd", categoryCode);
        }

        return publicWebResourceClient.post()
                .uri("/" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PublicResourceResponse.class)
                .doOnError(e ->
                        log.error("API 호출 실패 (category={}, page={})",
                                categoryCode, pageNo, e)
                );
    }
}