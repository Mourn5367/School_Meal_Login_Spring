package kr.ac.kopo.su.school_meal_login.controller;

import kr.ac.kopo.su.school_meal_login.dto.*;
import kr.ac.kopo.su.school_meal_login.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("loginRequest", new LoginRequestDto());
        if (error != null) {
            model.addAttribute("error", "로그인에 실패했습니다. 사용자명과 비밀번호를 확인해주세요.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequestDto loginRequest,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            LoginResponseDto response = authService.login(loginRequest);

            if (response != null && response.isSuccess()) {
                // 세션에 사용자 정보 저장
                session.setAttribute("SESSION_ID", response.getSessionId());
                session.setAttribute("CURRENT_USER", response.getUser());

                log.info("로그인 성공: {}", response.getUser().getUsername());

                return "redirect:/menu";
            } else {
                model.addAttribute("error", response != null ? response.getMessage() : "로그인에 실패했습니다.");
                return "auth/login";
            }
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequestDto registerRequest,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            ApiResponseDto<UserDto> response = authService.register(registerRequest);

            if (response != null && response.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
                return "redirect:/login";
            } else {
                model.addAttribute("error", response != null ? response.getMessage() : "회원가입에 실패했습니다.");
                return "auth/register";
            }
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String sessionId = (String) session.getAttribute("SESSION_ID");
            if (sessionId != null) {
                authService.logout(sessionId);
            }

            session.invalidate();
            redirectAttributes.addFlashAttribute("success", "로그아웃되었습니다.");
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/menu";
    }
}