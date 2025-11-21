package com.study.jwtauth.domain.comment;

import lombok.Getter;

@Getter
public enum CommentStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String description;

    CommentStatus(String description) {
        this.description = description;
    }
}
