package com.study.jwtauth.domain.post;

/**
 * 게시글 상태 Enum
 * - 게시글의 생명주기 상태를 표현
 */
public enum PostStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}