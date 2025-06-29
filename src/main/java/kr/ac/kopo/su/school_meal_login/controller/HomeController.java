package kr.ac.kopo.su.school_meal_login.controller;

import kr.ac.kopo.su.school_meal_login.dto.MenuDto;
import kr.ac.kopo.su.school_meal_login.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MenuService menuService;

    @GetMapping("/menu")
    public String menuPage(Model model) {
        try {
            List<MenuDto> menus = menuService.getWeeklyMenus();

            // 날짜별로 그룹화하고 정렬
            Map<String, List<MenuDto>> groupedMenus = menus.stream()
                    .filter(menu -> {
                        // 오늘 이후의 메뉴만 표시 (주말 제외)
                        try {
                            LocalDate menuDate = LocalDate.parse(menu.getDate());
                            LocalDate today = LocalDate.now();
                            int dayOfWeek = menuDate.getDayOfWeek().getValue();
                            return !menuDate.isBefore(today) && dayOfWeek < 6; // 월-금만
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.groupingBy(MenuDto::getDate))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));

            model.addAttribute("groupedMenus", groupedMenus);
            model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("M월 d일 (E)"));

        } catch (Exception e) {
            log.error("메뉴 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "메뉴를 불러오는데 실패했습니다.");
        }

        return "menu/list";
    }
}
