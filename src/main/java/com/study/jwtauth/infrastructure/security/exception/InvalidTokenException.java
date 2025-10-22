package com.study.jwtauth.infrastructure.security.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException(Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, cause);
    }
}
