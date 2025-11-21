package com.study.jwtauth.domain.comment.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class InvalidCommentContentException extends BusinessException {
    public InvalidCommentContentException(){
        super(ErrorCode.INVALID_COMMENT_CONTENT);
    }
    public InvalidCommentContentException(String message) {
        super(ErrorCode.INVALID_COMMENT_CONTENT ,message);
    }
}
