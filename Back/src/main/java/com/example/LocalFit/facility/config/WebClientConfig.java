package com.example.LocalFit.facility.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final int CODECS_SIZE = 100 * 1024 * 1024; // 100MB
    private final ApiProperties apiProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiProperties.getUrl()) // 기본 API URL 설정
                .codecs(config -> config.defaultCodecs().maxInMemorySize(CODECS_SIZE))
                .build();
    }
}
