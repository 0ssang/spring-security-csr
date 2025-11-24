package com.study.jwtauth.presentataion.dto.response;

import com.study.jwtauth.domain.comment.Comment;
import com.study.jwtauth.domain.user.User;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long postId,
        Long authorId,
        String authorNickname,
        Long parentCommentId,
        Integer depth,
        Long replyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getAuthorId(),
                null,
                comment.getParentCommentId(),
                comment.getDepth(),
                null,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static CommentResponse of(Comment comment, User author){
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getAuthorId(),
                author != null ? author.getNickname() : "알 수 없음",
                comment.getParentCommentId(),
                comment.getDepth(),
                null,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static CommentResponse of(Comment comment, User author, Long replyCount){
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getAuthorId(),
                author != null ? author.getNickname() : "알 수 없음",
                comment.getParentCommentId(),
                comment.getDepth(),
                replyCount,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
