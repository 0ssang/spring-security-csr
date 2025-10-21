package com.study.jwtauth.infrastructure.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.jwtauth.domain.common.exception.ErrorCode;
import com.study.jwtauth.presentataion.dto.common.ExceptionDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Security 관련 에러 응답을 생성하는 유틸리티 클래스
 * Filter 단계에서 발생하는 인증/인가 예외에 대한 JSON 응답 생성
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ApiResponse 형식으로 에러 응답 전송
     */
    public static void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
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
