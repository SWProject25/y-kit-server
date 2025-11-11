package com.twojz.y_kit.external.vworld.client;

import com.twojz.y_kit.external.vworld.dto.VWorldRegionApiResponse;
import com.twojz.y_kit.external.vworld.dto.VWorldRegionApiResponse.VWorldRegionItem;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class VWorldClient {
    private final WebClient vWorldClient;

    @Value("${vworld.api.key}")
    private String apiKey;

    @Value("${service.domain.url}")
    private String serviceDomain;

    public List<VWorldRegionItem> fetchRegions(String endpoint, String admCode) {
        return fetchAllPages(endpoint, admCode, 1, new ArrayList<>());
    }

    private List<VWorldRegionItem> fetchAllPages(String endpoint, String admCode, int pageNo, List<VWorldRegionItem> accumulated) {
        VWorldRegionApiResponse response = fetchPage(endpoint, admCode, pageNo);

        if (response == null ||
                response.getAdmVOList() == null ||
                response.getAdmVOList().getAdmVOList() == null ||
                response.getAdmVOList().getAdmVOList().isEmpty()) {
            return accumulated;
        }

        List<VWorldRegionItem> currentPage = response.getAdmVOList().getAdmVOList();
        accumulated.addAll(currentPage);

        int totalCount = Integer.parseInt(response.getAdmVOList().getTotalCount());
        int totalPages = (int) Math.ceil((double) totalCount / 100);

        if (pageNo < totalPages) {
            return fetchAllPages(endpoint, admCode, pageNo + 1, accumulated);
        }

        return accumulated;
    }

    private VWorldRegionApiResponse fetchPage(String endpoint, String admCode, int pageNo) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath(endpoint)
                .queryParam("key", apiKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", 100)
                .queryParam("domain", serviceDomain)
                .queryParam("format", "json");

        if (admCode != null && !admCode.isEmpty()) {
            uriBuilder.queryParam("admCode", admCode);
        }

        return vWorldClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .bodyToMono(VWorldRegionApiResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof java.io.IOException))
                .block();
    }
}