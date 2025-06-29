// AuthService.java - 디버깅 로그 추가
package kr.ac.kopo.su.school_meal_login.service;

import kr.ac.kopo.su.school_meal_login.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("=== 로그인 시도 ===");
        log.info("사용자명: {}", loginRequest.getUsername());

        try {
            // 요청 데이터 로깅
            log.info("백엔드 로그인 API 호출: /auth/login");

            // API 호출
            Map<String, Object> response = webClient.post()
                    .uri("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(loginRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("백엔드 API 응답 받음: {}", response);

            if (response != null) {
                // 응답 구조 확인
                boolean success = response.containsKey("message") &&
                        "Login successful".equals(response.get("message"));

                if (success && response.containsKey("user") && response.containsKey("session_token")) {
                    // 성공 응답 처리
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) response.get("user");
                    String sessionToken = (String) response.get("session_token");

                    UserDto user = convertToUserDto(userMap);

                    log.info("로그인 성공 - 사용자: {}, 세션토큰: {}", user.getUsername(), sessionToken != null ? "있음" : "없음");

                    return new LoginResponseDto(true, "로그인 성공", user, sessionToken);
                } else {
                    // 실패 응답 처리
                    String errorMessage = (String) response.getOrDefault("error", "로그인에 실패했습니다.");
                    log.warn("로그인 실패 - 메시지: {}", errorMessage);
                    return new LoginResponseDto(false, errorMessage, null, null);
                }
            } else {
                log.error("백엔드에서 null 응답 받음");
                return new LoginResponseDto(false, "서버 응답이 없습니다.", null, null);
            }

        } catch (WebClientResponseException e) {
            log.error("로그인 API 호출 실패 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());

            // 401 Unauthorized인 경우
            if (e.getStatusCode().value() == 401) {
                return new LoginResponseDto(false, "사용자명 또는 비밀번호가 잘못되었습니다.", null, null);
            }

            return new LoginResponseDto(false, "로그인 서비스에 연결할 수 없습니다.", null, null);
        } catch (Exception e) {
            log.error("로그인 중 예외 발생: ", e);
            return new LoginResponseDto(false, "로그인 처리 중 오류가 발생했습니다.", null, null);
        }
    }

    public ApiResponseDto<UserDto> register(RegisterRequestDto registerRequest) {
        log.info("=== 회원가입 시도 ===");
        log.info("사용자명: {}, 이메일: {}", registerRequest.getUsername(), registerRequest.getEmail());

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(registerRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("회원가입 API 응답: {}", response);

            if (response != null && "User registered successfully".equals(response.get("message"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) response.get("user");
                UserDto user = convertToUserDto(userMap);

                log.info("회원가입 성공 - 사용자: {}", user.getUsername());
                return new ApiResponseDto<>(true, "회원가입이 완료되었습니다.", user, null);
            } else {
                String errorMessage = (String) response.getOrDefault("error", "회원가입에 실패했습니다.");
                log.warn("회원가입 실패 - 메시지: {}", errorMessage);
                return new ApiResponseDto<>(false, errorMessage, null, null);
            }

        } catch (WebClientResponseException e) {
            log.error("회원가입 API 호출 실패 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 409) {
                return new ApiResponseDto<>(false, "이미 사용 중인 사용자명 또는 이메일입니다.", null, null);
            }

            return new ApiResponseDto<>(false, "회원가입 서비스에 연결할 수 없습니다.", null, e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 예외 발생: ", e);
            return new ApiResponseDto<>(false, "회원가입 처리 중 오류가 발생했습니다.", null, e.getMessage());
        }
    }

    public boolean logout(String sessionId) {
        log.info("=== 로그아웃 시도 ===");
        log.info("세션 ID: {}", sessionId != null ? "있음" : "없음");

        try {
            Map<String, String> requestBody = Map.of("session_token", sessionId);

            Map<String, Object> response = webClient.post()
                    .uri("/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            boolean success = response != null && "Logout successful".equals(response.get("message"));
            log.info("로그아웃 결과: {}", success ? "성공" : "실패");

            return success;
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    // UserDto 변환 헬퍼 메서드
    private UserDto convertToUserDto(Map<String, Object> userMap) {
        UserDto user = new UserDto();

        if (userMap.containsKey("id")) {
            user.setId(((Number) userMap.get("id")).longValue());
        }
        if (userMap.containsKey("username")) {
            user.setUsername((String) userMap.get("username"));
        }
        if (userMap.containsKey("email")) {
            user.setEmail((String) userMap.get("email"));
        }

        return user;
    }
}