package com.study.jwtauth.domain.comment;

import com.study.jwtauth.domain.comment.exception.CommentAccessDeniedException;
import com.study.jwtauth.domain.comment.exception.InvalidCommentContentException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    private static final int MIN_CONTENT_LENGTH = 1;
    private static final int MAX_CONTENT_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long authorId;

    @Column
    private Long parentCommentId;

    @Column(nullable = false)
    private Integer depth = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(String content, Long postId, Long authorId, Long parentCommentId, Integer depth) {
        validateComment(content);

        this.content = content;
        this.postId = postId;
        this.authorId = authorId;
        this.parentCommentId = parentCommentId;
        this.depth = depth;
        this.status = CommentStatus.ACTIVE;
    }

    public static Comment createComment(String content, Long postId, Long authorId){
        return Comment.builder()
                .content(content)
                .postId(postId)
                .authorId(authorId)
                .parentCommentId(null)
                .depth(0)
                .build();
    }

    public static Comment createReply(String content, Long postId, Long authorId, Long parentCommentId){
        return Comment.builder()
                .content(content)
                .postId(postId)
                .authorId(authorId)
                .parentCommentId(parentCommentId)
                .depth(1)
                .build();
    }

    public void update(String content, Long requestUserId){
        validateAuthorId(requestUserId);
        validateComment(content);

        this.content = content;
    }

    public void delete(Long requestUserId){
        validateAuthorId(requestUserId);

        this.status = CommentStatus.DELETED;
    }

    public boolean isActive(){
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean isAuthor(Long requestUserId){
        return this.authorId.equals(requestUserId);
    }

    public boolean isComment(){
        return this.parentCommentId == null && this.depth == 0;
    }

    public boolean isReply(){
        return this.parentCommentId != null && this.depth == 1;
    }


    private void validateAuthorId(Long userId) {
        if(!isAuthor(userId)){
            throw new CommentAccessDeniedException();
        }
    }

    private static void validateComment(String content) {
        if(Objects.isNull(content) || content.isBlank()){
            throw new InvalidCommentContentException("댓글 내용은 필수 입니다.");
        }

        if(content.length() < MIN_CONTENT_LENGTH || content.length() > MAX_CONTENT_LENGTH){
            throw new InvalidCommentContentException("댓글의 내용은 1자 ~ 1000자 사이여야 합니다.");
        }
    }
}
