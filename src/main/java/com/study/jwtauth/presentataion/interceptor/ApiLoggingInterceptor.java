package com.study.jwtauth.presentataion.interceptor;

import com.study.jwtauth.infrastructure.logging.StructuredLogger;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * API 요청/응답 로깅 인터셉터
 * 구조화된 JSON 로그를 생성하여 모든 API 호출을 기록
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청 시작 시간 기록
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        // 인증 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = extractUserInfo(authentication);

        // 구조화된 로그로 요청 정보 기록
        // requestId, clientIp는 MdcLoggingFilter에서 이미 설정됨
        StructuredLogger.logApiRequest(
                apiLogger,
                request.getMethod(),
                request.getRequestURI(),
                userInfo.userId(),
                userInfo.email(),
                request.getHeader("User-Agent")
        );

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 요청 처리 완료 시점
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 응답 시간 계산
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = (startTime != null) ? System.currentTimeMillis() - startTime : 0;

        // 인증 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = extractUserInfo(authentication);

        // 구조화된 로그로 응답 정보 기록
        StructuredLogger.logApiResponse(
                apiLogger,
                request.getMethod(),
                request.getRequestURI(),
                userInfo.userId(),
                userInfo.email(),
                response.getStatus(),
                duration,
                ex != null ? ex.getMessage() : null
        );
    }

    /**
     * 사용자 정보 추출
     */
    private UserInfo extractUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new UserInfo(null, "Anonymous");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return new UserInfo(userDetails.getId(), userDetails.getEmail());
        }

        return new UserInfo(null, authentication.getName());
    }

    /**
     * 사용자 정보 레코드
     */
    private record UserInfo(Long userId, String email) {
    }
}
