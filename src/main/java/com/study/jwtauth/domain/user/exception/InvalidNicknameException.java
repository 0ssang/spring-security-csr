package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidNicknameException extends BusinessException {

    public InvalidNicknameException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidNicknameException(ErrorCode errorCode, String nickname) {
        super(errorCode, String.format("%s (입력값: %s)", errorCode.getMessage(), nickname));
    }
}
