package kr.ac.kopo.su.school_meal_login.service;

import kr.ac.kopo.su.school_meal_login.dto.MenuDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final WebClient webClient;

    public List<MenuDto> getWeeklyMenus() {
        try {
            // 백엔드 API에서 메뉴 데이터 조회
            List<MenuDto> rawMenus = webClient.get()
                    .uri("/menu")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MenuDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (rawMenus == null) {
                return new ArrayList<>();
            }

            // Flutter의 구조에 맞게 변환 (각 날짜별로 아침, 점심, 저녁을 분리)
            List<MenuDto> processedMenus = new ArrayList<>();

            for (MenuDto menu : rawMenus) {
                // 아침 메뉴 추가
                if (menu.getBreakfast() != null && !menu.getBreakfast().trim().isEmpty()) {
                    MenuDto breakfast = new MenuDto();
                    breakfast.setId(menu.getId() * 3 + 1); // 고유 ID 생성
                    breakfast.setDate(menu.getDate());
                    breakfast.setMealType("아침");
                    breakfast.setContent(menu.getBreakfast());
                    breakfast.setCreatedAt(menu.getCreatedAt());
                    processedMenus.add(breakfast);
                }

                // 점심 메뉴 추가
                if (menu.getLunch() != null && !menu.getLunch().trim().isEmpty()) {
                    MenuDto lunch = new MenuDto();
                    lunch.setId(menu.getId() * 3 + 2);
                    lunch.setDate(menu.getDate());
                    lunch.setMealType("점심");
                    lunch.setContent(menu.getLunch());
                    lunch.setCreatedAt(menu.getCreatedAt());
                    processedMenus.add(lunch);
                }

                // 저녁 메뉴 추가
                if (menu.getDinner() != null && !menu.getDinner().trim().isEmpty()) {
                    MenuDto dinner = new MenuDto();
                    dinner.setId(menu.getId() * 3 + 3);
                    dinner.setDate(menu.getDate());
                    dinner.setMealType("저녁");
                    dinner.setContent(menu.getDinner());
                    dinner.setCreatedAt(menu.getCreatedAt());
                    processedMenus.add(dinner);
                }
            }

            return processedMenus;

        } catch (WebClientResponseException e) {
            log.error("메뉴 조회 API 오류: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("메뉴 조회 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
