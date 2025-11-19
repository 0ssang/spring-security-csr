package com.study.jwtauth.presentataion.dto.response;

import com.study.jwtauth.domain.postlike.PostLike;
import com.study.jwtauth.domain.user.User;

import java.time.LocalDateTime;

/**
 * 게시글 좋아요 응답 DTO
 *
 * 사용처:
 * - 좋아요 누른 사용자 목록 조회 시
 */
public record PostLikeResponse(
        Long id,
        Long postId,
        Long userId,
        String userNickname,
        LocalDateTime likedAt
) {
    public static PostLikeResponse from(PostLike postLike) {
        return new PostLikeResponse(
                postLike.getId(),
                postLike.getPostId(),
                postLike.getUserId(),
                null,
                postLike.getLikedAt()
        );
    }

    public static PostLikeResponse of(PostLike postLike, String userNickname) {
        return new PostLikeResponse(
                postLike.getId(),
                postLike.getPostId(),
                postLike.getUserId(),
                userNickname,
                postLike.getLikedAt()
        );
    }

    public static PostLikeResponse of(PostLike postLike, User user) {
        return new PostLikeResponse(
                postLike.getId(),
                postLike.getPostId(),
                postLike.getUserId(),
                user.getNickname(),
                postLike.getLikedAt()
        );
    }
}
