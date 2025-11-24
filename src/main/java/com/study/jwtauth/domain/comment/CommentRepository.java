package com.study.jwtauth.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 조회 (댓글 ID, 상태)
    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    // 게시글의 활성 댓글 조회
    @Query("""
    select c from Comment c
    where c.postId = :postId and c.parentCommentId is null and c.status = :status
    order by c.createdAt desc 
    """)
    Page<Comment> findCommentsByPostIdAndStatus(@Param("postId") Long postId, @Param("status") CommentStatus status, Pageable pageable);

    // 게시글의 댓글의 답글 목록 조회
    @Query("""
    select c from Comment c
    where c.parentCommentId = :parentCommentId and c.status = :status
    order by c.createdAt asc
    """)
    Page<Comment> findRepliesByParentCommentIdAndStatus(@Param("parentCommentId") Long parentCommentId, @Param("status") CommentStatus status, Pageable pageable);

    // 특정 사용자의 활성 댓글 조회
    @Query("""
    select c from Comment c
    where c.authorId = :authorId and c.status = :status
    order by c.createdAt desc 
    """)
    Page<Comment> findCommentsByAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") CommentStatus status, Pageable pageable);

    // 특정 게시물의 활성 댓글 수 조회(답글 포함)
    long countByPostIdAndStatus(Long postId, CommentStatus status);

    // 특정 게시물의 답글 개수 조회
    long countByParentCommentIdAndStatus(Long parentCommentId, CommentStatus status);
}
