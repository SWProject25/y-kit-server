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
    public WebClient policyClient(@Value("${POLICY_API_URL}") String policyUrl) {
        return createWebClient(policyUrl);
    }

    @Bean
    public WebClient publicWebResourceClient(@Value("${PUBLIC_RESOURCE_API_URL}") String url) {
        return createWebClient(url);
    }

    @Bean
    public WebClient vWorldClient(@Value("${VWORLD_API_URL}") String vWorldUrl) {
        return createWebClient(vWorldUrl);
    }

    @Bean
    public WebClient openAIClient(@Value("${OPENAI_API_BASE_URL}") String openAIUrl,
                                  @Value("${OPENAI_API_KEY}") String apiKey) {
        return createWebClient(openAIUrl)
                .mutate()
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(Duration.ofSeconds(60));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}