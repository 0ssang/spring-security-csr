package com.study.jwtauth.presentataion.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        @JsonIgnore HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDto error
) {
    // 200 OK - 조회, 수정, 삭제 성공
    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    // 201 Created - 생성 성공
    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(final HttpStatus status, final ExceptionDto error) {
        return new ApiResponse<>(status, false, null, error);
    }
}
