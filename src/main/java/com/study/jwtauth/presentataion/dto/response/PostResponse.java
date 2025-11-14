package com.study.jwtauth.presentataion.dto.response;

import com.study.jwtauth.domain.post.Post;
import com.study.jwtauth.domain.user.User;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        Integer likeCount,
        Integer viewCount,
        boolean isPopular,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                null,
                post.getLikeCount(),
                post.getViewCount(),
                post.isPopular(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static PostResponse of(Post post, String authorNickname) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                authorNickname,
                post.getLikeCount(),
                post.getViewCount(),
                post.isPopular(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static PostResponse of(Post post, User author) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                author.getNickname(),
                post.getLikeCount(),
                post.getViewCount(),
                post.isPopular(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}