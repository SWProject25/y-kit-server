package com.twojz.y_kit.global.security;

import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
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

    @Bean
    public WebClient vWorldClient(@Value("${vworld.api.url}") String vWorldUrl) {
        return createWebClient(vWorldUrl);
    }

    @Bean
    public WebClient openAIClient(@Value("${openai.api.base-url}") String openAIUrl,
                                  @Value("${openai.api.key}") String apiKey) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(Duration.ofSeconds(60));

        return createWebClient(openAIUrl)
                .mutate()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private WebClient createWebClient(String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)).build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .baseUrl(baseUrl)
                .build();
    }
}