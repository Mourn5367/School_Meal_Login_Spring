package kr.ac.kopo.su.school_meal_login.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Configuration
public class WebConfig {

    @Value("${api.backend.url}")
    private String backendUrl;

    @Value("${api.backend.timeout:30s}")
    private Duration timeout;

    @Bean
    public WebClient webClient() {
        log.info("WebClient 설정 - 백엔드 URL: {}, 타임아웃: {}", backendUrl, timeout);

        return WebClient.builder()
                .baseUrl(backendUrl)
                .defaultHeader("Accept", "application/json;charset=UTF-8")
                .defaultHeader("Content-Type", "application/json;charset=UTF-8")
                .filter(logRequest())
                .filter(logResponse())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }

    // 요청 로깅 필터
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("API 요청: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    log.debug("요청 헤더: {}={}", name, values));
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    // 응답 로깅 필터
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("API 응답: 상태코드={}", clientResponse.statusCode());
            if (!clientResponse.statusCode().is2xxSuccessful()) {
                log.error("API 오류 응답: {}", clientResponse.statusCode());
            }
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}