package com.study.jwtauth.domain.post.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class PostAccessDeniedException extends BusinessException {

    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED);
    }

    public PostAccessDeniedException(String message) {
        super(ErrorCode.POST_ACCESS_DENIED, message);
    }
}
