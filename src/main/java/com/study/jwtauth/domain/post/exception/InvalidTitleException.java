package com.study.jwtauth.domain.post.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidTitleException extends BusinessException {

    public InvalidTitleException(){
        super(ErrorCode.POST_NOT_FOUND);
    }

    public InvalidTitleException(String message) {
        super(ErrorCode.INVALID_POST_TITLE, message);
    }
}
