package com.study.jwtauth.infrastructure.config;

import com.study.jwtauth.infrastructure.security.jwt.JwtAccessDeniedHandler;
import com.study.jwtauth.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.study.jwtauth.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.study.jwtauth.infrastructure.security.jwt.JwtProvider;
import com.study.jwtauth.infrastructure.security.oauth2.CustomOAuth2UserService;
import com.study.jwtauth.infrastructure.security.oauth2.CustomOidcUserService;
import com.study.jwtauth.infrastructure.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    /**
     * CORS 설정
     * 프론트엔드(Vue.js)에서 백엔드 API 호출을 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 URL)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",      // Vue.js 개발 서버
                "http://localhost:5173",      // Vite 기본 포트
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // 브라우저에 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Form Login 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // 인증 및 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // Public 엔드포인트
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // OAuth2/OIDC 사용자 정보 로드 서비스 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)           // Kakao, Naver (OAuth2)
                                .oidcUserService(customOidcUserService)         // Google (OIDC)
                        )
                        // 로그인 성공 핸들러 (JWT 발급)
                        .successHandler(oAuth2SuccessHandler)
                )

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
