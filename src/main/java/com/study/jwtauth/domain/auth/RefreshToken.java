package com.study.jwtauth.domain.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;

/**
 * Redis에 저장되는 Refresh Token 엔티티
 * - email을 키로 사용
 * - TTL(Time To Live)로 자동 만료 관리
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private String email;

    private String token;

    private LocalDateTime expiresAt;

    @TimeToLive
    private Long ttl; // 초 단위

    public static RefreshToken of(String email, String token, Long expirationMs) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
        Long ttl = expirationMs / 1000; // 밀리초 → 초 변환

        return new RefreshToken(email, token, expiresAt, ttl);
    }

    /**
     * 토큰 갱신
     */
    public RefreshToken updateToken(String newToken, Long expirationMs) {
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
        Long newTtl = expirationMs / 1000;

        return new RefreshToken(this.email, newToken, newExpiresAt, newTtl);
    }
}
