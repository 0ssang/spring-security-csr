package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.domain.exception.ErrorCode;
import com.study.jwtauth.infrastructure.logging.StructuredLogger;
import com.study.jwtauth.infrastructure.security.util.SecurityResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증된 사용자가 권한이 없는 리소스에 접근할 때 403 에러를 반환하는 핸들러
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 구조화된 로그로 권한 없음 기록
        StructuredLogger.logSecurityError(
                log,
                request.getRequestURI(),
                "AccessDeniedException",
                "Access denied: " + accessDeniedException.getMessage()
        );

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        SecurityResponseUtil.sendErrorResponse(response, errorCode);
    }
}
