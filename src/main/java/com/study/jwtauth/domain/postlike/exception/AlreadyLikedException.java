package com.study.jwtauth.domain.postlike.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

/**
 * 이미 좋아요를 누른 게시글에 다시 좋아요를 시도할 때 발생하는 예외
 */
public class AlreadyLikedException extends BusinessException {

    public AlreadyLikedException() {
        super(ErrorCode.ALREADY_LIKED);
    }

    public AlreadyLikedException(String message) {
        super(ErrorCode.ALREADY_LIKED, message);
    }
}