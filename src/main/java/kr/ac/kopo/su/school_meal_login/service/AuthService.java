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
public class AuthService {

    private final WebClient webClient;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        try {
            return webClient.post()
                    .uri("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(loginRequest)
                    .retrieve()
                    .bodyToMono(LoginResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("로그인 실패: {}", e.getMessage());
            return new LoginResponseDto(false, "로그인에 실패했습니다.", null, null);
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage());
            return new LoginResponseDto(false, "서버 오류가 발생했습니다.", null, null);
        }
    }

    public ApiResponseDto<UserDto> register(RegisterRequestDto registerRequest) {
        try {
            return webClient.post()
                    .uri("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(registerRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<UserDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return new ApiResponseDto<>(false, "회원가입에 실패했습니다.", null, e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            return new ApiResponseDto<>(false, "서버 오류가 발생했습니다.", null, e.getMessage());
        }
    }

    public boolean logout(String sessionId) {
        try {
            ApiResponseDto<?> response = webClient.post()
                    .uri("/auth/logout")
                    .header("Session-ID", sessionId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<?>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
