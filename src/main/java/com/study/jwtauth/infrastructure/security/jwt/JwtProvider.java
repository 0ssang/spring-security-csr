package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.domain.user.Role;
import com.study.jwtauth.infrastructure.config.JwtProperties;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.infrastructure.security.exception.ExpiredTokenException;
import com.study.jwtauth.infrastructure.security.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId, String email, String nickname, String role) {
        long now = System.currentTimeMillis();
        Date expiresAt = new Date(now + accessTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim(AUTHORITIES_KEY, role)
                .issuedAt(new Date(now))
                .expiration(expiresAt)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String email) {
        long now = System.currentTimeMillis();
        Date expiresAt = new Date(now + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(expiresAt)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new InvalidTokenException("권한 정보가 없는 토큰입니다.");
        }

        // Claims에서 사용자 정보 추출
        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        String nickname = claims.get("nickname", String.class);
        String roleString = claims.get(AUTHORITIES_KEY, String.class);
        Role role = Role.valueOf(roleString);

        // CustomUserDetails 생성
        CustomUserDetails principal = CustomUserDetails.of(userId, email, nickname, role);

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("잘못된 JWT 서명입니다.", e);
            throw new InvalidTokenException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다.", e);
            throw new ExpiredTokenException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 JWT 토큰입니다.", e);
            throw new InvalidTokenException("지원하지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.debug("JWT 토큰이 잘못되었습니다.", e);
            throw new InvalidTokenException("JWT 토큰이 잘못되었습니다.");
        }
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Access Token 만료 시간 반환
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Refresh Token 만료 시간 반환
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
