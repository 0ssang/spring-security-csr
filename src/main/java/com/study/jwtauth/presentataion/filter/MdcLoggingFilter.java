package com.study.jwtauth.presentataion.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC 로깅 필터
 * 요청의 가장 처음에 실행되어 전역 컨텍스트(requestId, clientIp)를 설정
 * ThreadLocal 기반으로 Filter → Interceptor → Controller → Service 모든 계층에서 접근 가능
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String CLIENT_IP_KEY = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 고유 요청 ID 생성 (전체 요청 추적용)
            MDC.put(REQUEST_ID_KEY, UUID.randomUUID().toString());

            // 2. 클라이언트 IP 설정
            MDC.put(CLIENT_IP_KEY, extractClientIp(request));

            // 다음 필터 체인 실행 (Security Filter, Interceptor 등)
            filterChain.doFilter(request, response);
        } finally {
            // 3. 요청 완료 후 반드시 MDC 정리 (메모리 누수 방지)
            MDC.clear();
        }
    }

    /**
     * 클라이언트 실제 IP 추출 (프록시/로드밸런서 고려)
     */
    private String extractClientIp(HttpServletRequest request) {
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

        // X-Forwarded-For는 "client, proxy1, proxy2" 형식일 수 있음
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
