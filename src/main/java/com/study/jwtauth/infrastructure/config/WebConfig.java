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
                        "/api/auth/signup"    // 회원가입만 제외 (민감 정보)
                        // 로그인/로그아웃은 인터셉터에 포함 (API 로그 + 인증 로그 분리)
                );
    }
}
