package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.domain.common.exception.BusinessException;
import com.study.jwtauth.domain.common.exception.ErrorCode;
import com.study.jwtauth.infrastructure.security.util.SecurityResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 401 에러를 반환하는 핸들러
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("인증되지 않은 사용자의 접근: {}", authException.getMessage());

        // JwtAuthenticationFilter에서 설정한 예외 확인
        Exception exception = (Exception) request.getAttribute("exception");

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // 토큰 관련 예외가 있는 경우 해당 에러 코드 사용
        if (exception instanceof BusinessException) {
            errorCode = ((BusinessException) exception).getErrorCode();
        }

        SecurityResponseUtil.sendErrorResponse(response, errorCode);
    }
}
