package com.study.jwtauth.infrastructure.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.jwtauth.domain.common.exception.ErrorCode;
import com.study.jwtauth.presentataion.dto.common.ExceptionDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증된 사용자가 권한이 없는 리소스에 접근할 때 403 에러를 반환하는 핸들러
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("접근 권한이 없는 사용자의 접근: {}", accessDeniedException.getMessage());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        // ApiResponse 형식으로 응답 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("data", null);
        responseBody.put("error", Map.of(
                "code", exceptionDto.code(),
                "message", exceptionDto.message()
        ));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
