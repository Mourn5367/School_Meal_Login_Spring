// MenuService.java - 유니코드 디코딩 처리 추가
package kr.ac.kopo.su.school_meal_login.service;

import kr.ac.kopo.su.school_meal_login.dto.MenuDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final WebClient webClient;

    public List<MenuDto> getWeeklyMenus() {
        log.info("=== 메뉴 조회 시작 ===");

        try {
            // 백엔드 API 호출
            List<Map<String, Object>> rawMenus = webClient.get()
                    .uri("/menu")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("백엔드 API 응답 받음. 데이터 개수: {}", rawMenus != null ? rawMenus.size() : 0);



            // 응답 구조 로깅
            if (!rawMenus.isEmpty()) {
                Map<String, Object> firstMenu = rawMenus.get(0);
                log.info("첫 번째 메뉴 항목 구조: {}", firstMenu.keySet());
                log.info("첫 번째 메뉴 원본 내용: {}", firstMenu.get("content"));
            }

            // 백엔드 API 응답 변환
            List<MenuDto> processedMenus = new ArrayList<>();

            for (Map<String, Object> menuMap : rawMenus) {
                MenuDto menu = convertApiResponseToMenu(menuMap);
                if (menu != null && isValidMenu(menu)) {
                    processedMenus.add(menu);
                    log.debug("메뉴 추가: {} {} - {}",
                            menu.getDate(), menu.getMealType(),
                            menu.getContent().length() > 50 ?
                                    menu.getContent().substring(0, 50) + "..." : menu.getContent());
                }
            }

            log.info("총 처리된 메뉴 항목 수: {}", processedMenus.size());
            return processedMenus;

        } catch (WebClientResponseException e) {
            log.error("메뉴 조회 API 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("메뉴 조회 중 예외 발생: ", e);
            return null;
        }
    }

    private MenuDto convertApiResponseToMenu(Map<String, Object> menuMap) {
        try {
            MenuDto menu = new MenuDto();

            // ID 설정
            if (menuMap.containsKey("id")) {
                menu.setId(((Number) menuMap.get("id")).longValue());
            }

            // 날짜 설정 및 변환
            if (menuMap.containsKey("date")) {
                String dateStr = convertDateFormat((String) menuMap.get("date"));
                menu.setDate(dateStr);
            }

            // 식사 타입 설정 및 디코딩
            if (menuMap.containsKey("meal_type")) {
                String mealType = decodeUnicodeString((String) menuMap.get("meal_type"));
                menu.setMealType(mealType);
            }

            // 메뉴 내용 설정 및 디코딩
            if (menuMap.containsKey("content")) {
                String content = decodeUnicodeString((String) menuMap.get("content"));
                // \r 문자 제거 및 정리
                content = cleanMenuContent(content);
                menu.setContent(content);
            }

            return menu;
        } catch (Exception e) {
            log.error("메뉴 변환 중 오류: ", e);
            return null;
        }
    }

    // 유니코드 문자열 디코딩
    private String decodeUnicodeString(String input) {
        if (input == null) return null;

        try {
            // 유니코드를 실제 한글로 변환
            Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
            Matcher matcher = pattern.matcher(input);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String hex = matcher.group(1);
                int codePoint = Integer.parseInt(hex, 16);
                matcher.appendReplacement(sb, String.valueOf((char) codePoint));
            }
            matcher.appendTail(sb);

            return sb.toString();
        } catch (Exception e) {
            log.warn("유니코드 디코딩 실패: {}", input);
            return input; // 디코딩 실패시 원본 반환
        }
    }

    // 메뉴 내용 정리
    private String cleanMenuContent(String content) {
        if (content == null) return null;

        return content
                .replace("\r", "") // \r 문자 제거
                .replace("\n", "") // \n 문자 제거
                .trim(); // 앞뒤 공백 제거
    }

    // 날짜 형식 변환 (GMT 형식 → YYYY-MM-DD)
    private String convertDateFormat(String dateStr) {
        try {
            if (dateStr == null) return null;

            // "Fri, 04 Jul 2025 00:00:00 GMT" → "2025-07-04"
            if (dateStr.contains("GMT")) {
                // GMT 형식 파싱
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.ENGLISH);
                LocalDate date = LocalDate.parse(dateStr, inputFormatter);
                return date.toString(); // YYYY-MM-DD 형식
            }

            return dateStr; // 이미 올바른 형식인 경우
        } catch (Exception e) {
            log.warn("날짜 변환 실패: {}", dateStr);
            return dateStr; // 변환 실패시 원본 반환
        }
    }

    // 유효한 메뉴인지 검사
    private boolean isValidMenu(MenuDto menu) {
        if (menu == null) return false;
        if (menu.getDate() == null || menu.getMealType() == null) return false;
        if (menu.getContent() == null || menu.getContent().trim().isEmpty()) return false;

        // "정보 없음" 등의 무효한 내용 제외
        String content = menu.getContent().toLowerCase();
        if (content.contains("정보 없음") || content.contains("선거일") || content.contains("현충일")) {
            return false;
        }

        // 오늘 이전 날짜와 주말 제외
        try {
            LocalDate menuDate = LocalDate.parse(menu.getDate());
            LocalDate today = LocalDate.now();
            int dayOfWeek = menuDate.getDayOfWeek().getValue();

            return !menuDate.isBefore(today) && dayOfWeek < 6; // 오늘 이후 평일만
        } catch (Exception e) {
            log.warn("날짜 검증 실패: {}", menu.getDate());
            return false;
        }
    }





}