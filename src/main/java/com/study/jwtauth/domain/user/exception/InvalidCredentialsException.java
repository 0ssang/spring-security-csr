package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
