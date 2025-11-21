package com.study.jwtauth.domain.comment.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

public class CommentAccessDeniedException extends BusinessException {
    public CommentAccessDeniedException(){
        super(ErrorCode.COMMENT_ACCESS_DENIED);
    }

    public CommentAccessDeniedException(String message) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, message);
    }
}
