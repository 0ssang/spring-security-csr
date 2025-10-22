package com.study.jwtauth.domain.user.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class DuplicateNicknameException extends BusinessException {

    public DuplicateNicknameException() {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }

    public DuplicateNicknameException(String nickname) {
        super(ErrorCode.DUPLICATE_NICKNAME, String.format("이미 사용 중인 닉네임입니다: %s", nickname));
    }
}
