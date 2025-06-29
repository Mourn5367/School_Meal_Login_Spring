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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final WebClient webClient;

    public List<PostDto> getAllPosts() {
        try {
            // 최근 게시글 목록 조회
            return webClient.get()
                    .uri("/posts")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PostDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("전체 게시글 조회 API 오류: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("전체 게시글 조회 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<PostDto> getPostsByMeal(String date, String mealType) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/posts")
                            .queryParam("date", date)
                            .queryParam("meal_type", mealType)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PostDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("메뉴별 게시글 조회 API 오류: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("메뉴별 게시글 조회 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public PostDto getPostDetail(Long postId) {
        try {
            // 게시글 상세 조회 API 호출
            Map<String, Object> response = webClient.get()
                    .uri("/posts/{id}", postId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null) {
                // 응답 구조에 따라 데이터 추출
                PostDto post = convertToPostDto(response);

                if (response.containsKey("comments")) {
                    List<CommentDto> comments = convertToCommentList((List<?>) response.get("comments"));
                    post.setComments(comments);
                }

                return post;
            }

            return null;

        } catch (WebClientResponseException e) {
            log.error("게시글 상세 조회 API 오류: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("게시글 상세 조회 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    public PostDto createPost(PostRequestDto postRequest, String date, String mealType, String sessionId) {
        try {
            // 게시글 생성 요청 데이터 준비
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", postRequest.getTitle());
            requestBody.put("content", postRequest.getContent());
            requestBody.put("author", postRequest.getAuthor());
            requestBody.put("meal_date", date);
            requestBody.put("meal_type", mealType);
            requestBody.put("session_token", sessionId);

            return webClient.post()
                    .uri("/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PostDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("게시글 작성 API 오류: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // Helper methods for data conversion
    private PostDto convertToPostDto(Map<String, Object> response) {
        PostDto post = new PostDto();

        if (response.containsKey("id")) {
            post.setId(((Number) response.get("id")).longValue());
        }
        if (response.containsKey("title")) {
            post.setTitle((String) response.get("title"));
        }
        if (response.containsKey("content")) {
            post.setContent((String) response.get("content"));
        }
        if (response.containsKey("author")) {
            post.setAuthor((String) response.get("author"));
        }
        if (response.containsKey("like_count")) {
            post.setLikeCount(((Number) response.get("like_count")).intValue());
        }
        if (response.containsKey("comment_count")) {
            post.setCommentCount(((Number) response.get("comment_count")).intValue());
        }

        return post;
    }

    private List<CommentDto> convertToCommentList(List<?> commentData) {
        List<CommentDto> comments = new ArrayList<>();

        for (Object item : commentData) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> commentMap = (Map<String, Object>) item;
                CommentDto comment = new CommentDto();

                if (commentMap.containsKey("id")) {
                    comment.setId(((Number) commentMap.get("id")).longValue());
                }
                if (commentMap.containsKey("content")) {
                    comment.setContent((String) commentMap.get("content"));
                }
                if (commentMap.containsKey("author")) {
                    comment.setAuthor((String) commentMap.get("author"));
                }
                if (commentMap.containsKey("like_count")) {
                    comment.setLikeCount(((Number) commentMap.get("like_count")).intValue());
                }

                comments.add(comment);
            }
        }

        return comments;
    }
}