package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.common.exception.BusinessException;
import com.study.jwtauth.domain.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }

    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL, String.format("이미 사용 중인 이메일입니다: %s", email));
    }
}
