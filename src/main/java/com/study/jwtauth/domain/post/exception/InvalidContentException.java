package com.study.jwtauth.domain.post.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidContentException extends BusinessException {

    public InvalidContentException() {
        super(ErrorCode.INVALID_POST_CONTENT);
    }

    public InvalidContentException(String message) {
        super(ErrorCode.INVALID_POST_CONTENT, message);
    }
}
