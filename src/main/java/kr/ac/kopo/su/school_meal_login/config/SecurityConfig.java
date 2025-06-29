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
                        // 정적 리소스와 로그인 관련 페이지는 모든 사용자에게 허용
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // 게시글 읽기는 모든 사용자에게 허용 (로그인 없이도 볼 수 있음)
                        .requestMatchers("/posts/*/view", "/posts").permitAll()
                        // 메뉴 페이지도 모든 사용자에게 허용
                        .requestMatchers("/menu").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/menu", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .addLogoutHandler(customLogoutHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // 세션 관리
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                );

        return http.build();
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            // 백엔드 API 로그아웃 호출을 위한 세션 ID 정리
            String sessionId = (String) request.getSession().getAttribute("SESSION_ID");
            if (sessionId != null) {
                // 여기서 백엔드 API 로그아웃 호출
                // AuthService.logout(sessionId) 호출 가능
                request.getSession().removeAttribute("SESSION_ID");
                request.getSession().removeAttribute("CURRENT_USER");
            }
        };
    }

    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }
}