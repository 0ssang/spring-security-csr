package com.study.jwtauth.presentataion.dto.response;

import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt
) {
    public static BoardResponse of(Long id, String title, String content, Long authorId, String authorNickname) {
        return new BoardResponse(id, title, content, authorId, authorNickname, LocalDateTime.now());
    }
}
