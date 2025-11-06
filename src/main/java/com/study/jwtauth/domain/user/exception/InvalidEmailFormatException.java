package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidEmailFormatException extends BusinessException {

    public InvalidEmailFormatException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidEmailFormatException(ErrorCode errorCode, String email) {
        super(errorCode, String.format("%s (입력값: %s)", errorCode.getMessage(), email));
    }
}
