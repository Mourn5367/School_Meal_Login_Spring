package kr.ac.kopo.su.school_meal_login.service;

import kr.ac.kopo.su.school_meal_login.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final WebClient webClient;

    public List<MenuDto> getMenus(String date) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/menu")
                            .queryParamIfPresent("date", java.util.Optional.ofNullable(date))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MenuDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("급식 메뉴 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
