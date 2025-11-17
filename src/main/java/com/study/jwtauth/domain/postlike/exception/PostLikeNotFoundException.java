package com.study.jwtauth.domain.postlike.exception;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;

/**
 * 좋아요 기록을 찾을 수 없을 때 발생하는 예외
 */
public class PostLikeNotFoundException extends BusinessException {

    public PostLikeNotFoundException() {
        super(ErrorCode.POST_LIKE_NOT_FOUND);
    }

    public PostLikeNotFoundException(String message) {
        super(ErrorCode.POST_LIKE_NOT_FOUND, message);
    }
}
