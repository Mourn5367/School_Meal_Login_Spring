package kr.ac.kopo.su.school_meal_login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

@Configuration
public class WebConfig {

    @Value("${api.backend.url}")
    private String backendUrl;

    @Value("${api.backend.timeout}")
    private Duration timeout;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(backendUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
}