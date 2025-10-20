package com.study.jwtauth.presentataion.exception;

import com.study.jwtauth.domain.common.exception.BusinessException;
import com.study.jwtauth.domain.common.exception.ErrorCode;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.common.ExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
    protected ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        // 필드 에러 메시지들을 하나의 문자열로 결합
        String fieldErrorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

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
    protected ApiResponse<Void> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        String fieldErrorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

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
    protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE;
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 지원하지 않는 HTTP 메서드 요청 시 발생하는 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);

        ErrorCode errorCode = e.getErrorCode();
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode.getCode(), e.getMessage());

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ExceptionDto exceptionDto = ExceptionDto.of(errorCode);

        return ApiResponse.error(errorCode.getStatus(), exceptionDto);
    }
}
