package com.study.jwtauth.domain.postlike;

import com.study.jwtauth.domain.post.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime likedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private PostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public static PostLike create(Long postId, Long userId) {
        return PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();
    }

    public boolean isLikedBy(Long userId){
        return this.userId.equals(userId);
    }
}