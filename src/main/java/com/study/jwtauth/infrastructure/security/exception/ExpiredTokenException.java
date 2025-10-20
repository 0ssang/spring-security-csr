package com.study.jwtauth.infrastructure.security.exception;

import com.study.jwtauth.domain.common.exception.BusinessException;
import com.study.jwtauth.domain.common.exception.ErrorCode;

public class ExpiredTokenException extends BusinessException {

    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }

    public ExpiredTokenException(String message) {
        super(ErrorCode.EXPIRED_TOKEN, message);
    }
}
