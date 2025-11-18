package com.study.jwtauth.domain.postlike;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 게시글에 특정 사용자가 좋아요했는지 조회
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 특정 게시글에 특정 사용자가 좋아요했는지 존재 여부 확인
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 특정 게시글에 특정 사용자의 좋아요 삭제
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);

    /**
     * 특정 게시글의 좋아요 목록 조회 (페이징)
     */
    Page<PostLike> findByPostId(Long postId, Pageable pageable);

    /**
     * 특정 사용자가 좋아요한 게시글 목록 조회 (페이징)
     */
    Page<PostLike> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 게시글의 좋아요 개수
     */
    long countByPostId(Long postId);
}