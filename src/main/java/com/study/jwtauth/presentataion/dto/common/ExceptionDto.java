package com.study.jwtauth.presentataion.dto.common;

import com.study.jwtauth.domain.common.exception.ErrorCode;

public record ExceptionDto(
        String code,
        String message
) {
    // ErrorCode를 받는 커스텀 생성자
    public ExceptionDto(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    // 정적 팩토리 메서드
    public static ExceptionDto of(ErrorCode errorCode) {
        return new ExceptionDto(errorCode);
    }

    public static ExceptionDto of(String code, String message) {
        return new ExceptionDto(code, message);
    }
}
