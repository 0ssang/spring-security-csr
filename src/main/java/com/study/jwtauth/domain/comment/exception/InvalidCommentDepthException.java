package com.study.jwtauth.domain.comment.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidCommentDepthException extends BusinessException {
    public InvalidCommentDepthException(){
        super(ErrorCode.INVALID_COMMENT_DEPTH);
    }
    public InvalidCommentDepthException(String message) {
        super(ErrorCode.INVALID_COMMENT_DEPTH, message);
    }
}
