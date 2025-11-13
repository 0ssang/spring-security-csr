package com.study.jwtauth.domain.post.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class PostNotFoundException extends BusinessException {

    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }

    public PostNotFoundException(String message){
        super(ErrorCode.POST_NOT_FOUND, message);
    }
}
