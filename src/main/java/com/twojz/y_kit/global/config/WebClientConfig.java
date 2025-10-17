package com.twojz.y_kit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient policyClient(@Value("${policy.api.url}") String policyUrl) {
        return createWebClient(policyUrl);
    }

    private WebClient createWebClient(String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs()
                            .maxInMemorySize(16 * 1024 * 1024);
                }).build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .baseUrl(baseUrl)
                .build();
    }
}