package com.study.jwtauth.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 기본 조회
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, PostStatus status, Pageable pageable);


    // 인기글 조회 (좋아요 기준)
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'ACTIVE' AND p.likeCount >= :threshold " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC ")
    Page<Post> findMostLikedPosts(@Param("threshold") int threshold, Pageable pageable);


    // 조회수 기준 조회
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'ACTIVE' " +
            "ORDER BY p.viewCount DESC, p.createdAt DESC")
    Page<Post> findMostViewedPosts(Pageable pageable);


    // 검색
    Page<Post> findByTitleContainingAndStatus(String keyword, PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'ACTIVE' AND " +
            "(p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable);


    // 통계/집계
    long countByAuthorIdAndStatus(Long authorId, PostStatus status);

    long countByStatus(PostStatus status);
}
