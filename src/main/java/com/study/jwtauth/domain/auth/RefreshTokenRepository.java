package com.study.jwtauth.domain.auth;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * RefreshToken Redis Repository
 * - email을 키로 조회, 저장, 삭제
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    /**
     * 이메일로 RefreshToken 조회
     */
    Optional<RefreshToken> findByEmail(String email);

    /**
     * RefreshToken 저장
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * 이메일로 RefreshToken 삭제
     */
    void deleteById(String email);
}
