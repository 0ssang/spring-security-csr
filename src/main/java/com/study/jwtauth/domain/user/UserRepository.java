package com.study.jwtauth.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /**
     * email로 사용자 조회 (providers fetch join)
     * 인증 시 providers를 함께 조회하여 N+1 문제 방지
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.providers WHERE u.email = :email")
    Optional<User> findByEmailWithProvider(@Param("email") String email);

    /**
     * email 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * nickname 중복 체크
     */
    boolean existsByNickname(String nickname);

    /**
     * provider와 providerId로 사용자 조회 (providers fetch join)
     * OAuth2 로그인 시 사용
     */
    @Query("SELECT u FROM User u JOIN FETCH u.providers p WHERE p.provider = :provider AND p.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
}
