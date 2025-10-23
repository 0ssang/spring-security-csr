package com.study.jwtauth.infrastructure.config;

import com.study.jwtauth.presentataion.interceptor.ApiLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor apiLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/api/**")  // /api로 시작하는 모든 요청
                .excludePathPatterns(
                        "/api/auth/login",    // 로그인은 AuthService에서 로깅
                        "/api/auth/signup"    // 회원가입은 제외
                );
    }
}
