package com.study.jwtauth.presentataion.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;
import com.study.jwtauth.infrastructure.logging.StructuredLogger;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.common.ExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 실패 시 발생하는 예외 처리
     * (@Valid, @Validated 어노테이션으로 검증 실패 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<Void> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        // 필드 에러 메시지들을 하나의 문자열로 결합
        String fieldErrorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        // 구조화된 로그 (스택트레이스 없이 필수 정보만)
        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "ValidationException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Validation failed: " + fieldErrorMessages,
                null  // 스택트레이스 불필요
        );

        ExceptionDto exceptionDto = ExceptionDto.of(
                errorCode.getCode(),
                fieldErrorMessages.isEmpty() ? errorCode.getMessage() : fieldErrorMessages
        );

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * @ModelAttribute 바인딩 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(BindException.class)
    protected ApiResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        String fieldErrorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "BindException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Bind failed: " + fieldErrorMessages,
                null
        );

        ExceptionDto exceptionDto = ExceptionDto.of(
                errorCode.getCode(),
                fieldErrorMessages.isEmpty() ? errorCode.getMessage() : fieldErrorMessages
        );

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE;

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "TypeMismatchException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Type mismatch: " + e.getMessage(),
                null
        );

        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 지원하지 않는 HTTP 메서드 요청 시 발생하는 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "MethodNotSupportedException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Method not allowed: " + e.getMethod(),
                null
        );

        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ApiResponse<Void> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "AccessDeniedException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Access denied",
                null
        );

        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "BusinessException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                e.getMessage(),
                null  // 비즈니스 예외는 예상된 예외이므로 스택트레이스 불필요
        );

        ExceptionDto exceptionDto = ExceptionDto.of(errorCode.getCode(), e.getMessage());

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        UserInfo userInfo = extractUserInfo();
        StructuredLogger.logError(
                log,
                "UnexpectedException",
                errorCode.getCode(),
                userInfo.userId(),
                userInfo.email(),
                request.getRequestURI(),
                "Unexpected error: " + e.getMessage(),
                e  // 예상치 못한 예외만 스택트레이스 포함 (logback에서 제한됨)
        );

        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 현재 인증된 사용자 정보 추출
     */
    private UserInfo extractUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new UserInfo(null, null);
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
