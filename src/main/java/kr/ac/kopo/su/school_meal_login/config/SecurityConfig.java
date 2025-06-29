package kr.ac.kopo.su.school_meal_login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 호출을 위해)
                .csrf(csrf -> csrf.disable())

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/menu", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/posts/*/view").permitAll()  // 게시글 읽기는 모든 사용자에게 허용
                        .anyRequest().authenticated()
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/posts", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // 로그아웃 설정 - 최신 방식 사용
                .logout(logout -> logout
                        .logoutUrl("/logout")  // AntPathRequestMatcher 대신 단순 문자열 사용
                        .logoutSuccessUrl("/")
                        .addLogoutHandler(customLogoutHandler())
                        .permitAll()
                )

                // 세션 관리
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            // 백엔드 API 로그아웃 호출을 위한 세션 ID 정리
            String sessionId = (String) request.getSession().getAttribute("API_SESSION_ID");
            if (sessionId != null) {
                // 여기서 백엔드 API 로그아웃 호출
                // AuthService.logout(sessionId) 호출 가능
                request.getSession().removeAttribute("API_SESSION_ID");
                request.getSession().removeAttribute("CURRENT_USER");
            }
        };
    }
}