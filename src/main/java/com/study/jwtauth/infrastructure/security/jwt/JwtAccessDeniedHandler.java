package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.domain.exception.ErrorCode;
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

        log.error("접근 권한이 없는 사용자의 접근: {}", accessDeniedException.getMessage());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        SecurityResponseUtil.sendErrorResponse(response, errorCode);
    }
}
