package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidPasswordFormatException extends BusinessException {

    public InvalidPasswordFormatException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidPasswordFormatException(ErrorCode errorCode, String password) {
        super(errorCode, String.format("%s (입력값 길이: %d)", errorCode.getMessage(), password.length()));
    }
}