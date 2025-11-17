package com.study.jwtauth.domain.postlike.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

/**
 * 자신의 게시글에 좋아요를 시도할 때 발생하는 예외
 */
public class CannotLikeOwnPostException extends BusinessException {

    public CannotLikeOwnPostException() {
        super(ErrorCode.CANNOT_LIKE_OWN_POST);
    }

    public CannotLikeOwnPostException(String message) {
        super(ErrorCode.CANNOT_LIKE_OWN_POST, message);
    }
}
