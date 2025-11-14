package com.study.jwtauth.domain.post;

import com.study.jwtauth.domain.post.exception.InvalidContentException;
import com.study.jwtauth.domain.post.exception.InvalidTitleException;
import com.study.jwtauth.domain.post.exception.PostAccessDeniedException;
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
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    private static final int MIN_TITLE_LENGTH = 1;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_CONTENT_LENGTH = 1;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int POPULAR_THRESHOLD = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public Post(String title, String content, Long authorId){
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = PostStatus.ACTIVE;
    }

    public static Post create(String title, String content, Long authorId) {
        return Post.builder()
                .title(title)
                .content(content)
                .authorId(authorId)
                .build();
    }

    public void update(String title, String content, Long requestUserId) {
        validateAuthor(requestUserId);
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
    }

    public void delete(Long requestUserId) {
        validateAuthor(requestUserId);

        this.status = PostStatus.DELETED;
    }

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    public void incrementLikeCount() {
        this.likeCount += 1;
    }

    public void decrementLikeCount() {
        if(this.likeCount > 0) {
            this.likeCount -= 1;
        }
    }

    public boolean isPopular() {
        return this.likeCount >= POPULAR_THRESHOLD;
    }

    public boolean isActive() {
        return this.status == PostStatus.ACTIVE;
    }

    public boolean isAuthor(Long userId) {
        return this.authorId.equals(userId);
    }

    private void validateAuthor(Long userId) {
        if(!isAuthor(userId)) {
            throw new PostAccessDeniedException();
        }
    }

    private static void validateTitle(String title) {
        if(Objects.isNull(title) || title.isBlank()) {
            throw new InvalidTitleException();
        }

        if(title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidTitleException("제목은 1자 이상 200자 이하여야 합니다.");
        }
    }

    private static void validateContent(String content) {
        if(Objects.isNull(content) || content.isBlank()) {
            throw new InvalidContentException();
        }

        if(content.length() < MIN_CONTENT_LENGTH || content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidContentException("본문은 1자 이상 10000자 이하여야 합니다.");
        }
    }
}