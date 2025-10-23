package com.study.jwtauth.presentataion.interceptor;

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
 * 로그인한 사용자의 모든 API 호출을 기록
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
        String userInfo = getUserInfo(authentication);

        // 요청 정보 로깅
        apiLogger.info("[REQ] {} {} | User: {} | IP: {} | UserAgent: {}",
                request.getMethod(),
                request.getRequestURI(),
                userInfo,
                getClientIp(request),
                request.getHeader("User-Agent"));

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
        String userInfo = getUserInfo(authentication);

        // 응답 정보 로깅
        if (ex != null) {
            apiLogger.error("[RES] {} {} | User: {} | Status: ERROR | Duration: {}ms | Exception: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    userInfo,
                    duration,
                    ex.getMessage());
        } else {
            apiLogger.info("[RES] {} {} | User: {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    userInfo,
                    response.getStatus(),
                    duration);
        }
    }

    /**
     * 사용자 정보 추출
     */
    private String getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Anonymous";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return String.format("%s(id=%d)", userDetails.getEmail(), userDetails.getId());
        }

        return authentication.getName();
    }

    /**
     * 클라이언트 IP 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
