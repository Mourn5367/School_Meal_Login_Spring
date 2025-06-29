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
public class CommentService {

    private final WebClient webClient;

    public CommentDto createComment(Long postId, CommentRequestDto commentRequest, String sessionId) {
        try {
            return webClient.post()
                    .uri("/posts/{postId}/comments", postId)
                    .header("Session-ID", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commentRequest)
                    .retrieve()
                    .bodyToMono(CommentDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("댓글 작성 실패: {}", e.getMessage());
            return null;
        }
    }

    public CommentDto updateComment(Long id, CommentRequestDto commentRequest, String sessionId) {
        try {
            return webClient.put()
                    .uri("/comments/{id}", id)
                    .header("Session-ID", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commentRequest)
                    .retrieve()
                    .bodyToMono(CommentDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean deleteComment(Long id, String sessionId) {
        try {
            ApiResponseDto<?> response = webClient.delete()
                    .uri("/comments/{id}", id)
                    .header("Session-ID", sessionId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<?>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("댓글 삭제 실패: {}", e.getMessage());
            return false;
        }
    }
}
