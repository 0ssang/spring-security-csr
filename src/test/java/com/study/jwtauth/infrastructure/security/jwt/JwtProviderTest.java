package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.domain.user.Role;
import com.study.jwtauth.infrastructure.config.JwtProperties;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.infrastructure.security.exception.ExpiredTokenException;
import com.study.jwtauth.infrastructure.security.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtProvider 단위 테스트
 *
 * 테스트 범위:
 * - Access Token 생성 및 검증
 * - Refresh Token 생성 및 검증
 * - 만료된 토큰 처리
 * - 잘못된 토큰 처리
 * - Claims 추출
 */
@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtProperties jwtProperties;

    // 테스트용 데이터
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "테스터";
    private static final String TEST_ROLE = "USER";

    /**
     * 각 테스트 실행 전 JwtProvider 초기화
     * @BeforeEach: 각 테스트 메서드 실행 전에 실행됨
     */
    @BeforeEach
    void setUp() {
        // JwtProperties 설정 (테스트용)
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdW5pdC10ZXN0aW5nLW11c3QtYmUtbG9uZy1lbm91Z2gtZm9yLWhzMjU2LWFsZ29yaXRobS10by13b3JrLXByb3Blcmx5");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));  // 15분
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));  // 7일

        // JwtProvider 생성
        jwtProvider = new JwtProvider(jwtProperties);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void createAccessToken_Success() {
        // given (준비): 테스트에 필요한 데이터 준비

        // when (실행): 실제 테스트할 메서드 실행
        String accessToken = jwtProvider.createAccessToken(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NICKNAME,
            TEST_ROLE
        );

        // then (검증): 결과가 예상대로인지 확인
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
        assertThat(accessToken.split("\\.")).hasSize(3); // JWT는 header.payload.signature 형식
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void createRefreshToken_Success() {
        // when
        String refreshToken = jwtProvider.createRefreshToken(TEST_EMAIL);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("유효한 Access Token 검증 성공")
    void validateToken_ValidAccessToken_Success() {
        // given
        String accessToken = jwtProvider.createAccessToken(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NICKNAME,
            TEST_ROLE
        );

        // when & then
        assertThatCode(() -> jwtProvider.validateToken(accessToken))
            .doesNotThrowAnyException(); // 예외가 발생하지 않아야 함
    }

    @Test
    @DisplayName("유효한 Refresh Token 검증 성공")
    void validateToken_ValidRefreshToken_Success() {
        // given
        String refreshToken = jwtProvider.createRefreshToken(TEST_EMAIL);

        // when & then
        assertThatCode(() -> jwtProvider.validateToken(refreshToken))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateToken_InvalidFormat_ThrowsException() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(invalidToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("잘못된 JWT");
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 검증 실패")
    void validateToken_InvalidSignature_ThrowsException() {
        // given: 다른 시크릿 키로 생성된 토큰
        String wrongSecret = "d3Jvbmctc2VjcmV0LWtleS1mb3ItdGVzdGluZy1tdXN0LWJlLWxvbmctZW5vdWdoLWZvci1oczI1Ni1hbGdvcml0aG0tdG8td29yay1wcm9wZXJseQ==";
        SecretKey wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(wrongSecret));

        String tokenWithWrongSignature = Jwts.builder()
            .subject(TEST_EMAIL)
            .claim("userId", TEST_USER_ID)
            .claim("nickname", TEST_NICKNAME)
            .claim("auth", TEST_ROLE)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 900000))
            .signWith(wrongKey, Jwts.SIG.HS256)
            .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(tokenWithWrongSignature))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("잘못된 JWT 서명");
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken_ThrowsException() {
        // given: 이미 만료된 토큰 생성 (만료 시간을 과거로 설정)
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));

        String expiredToken = Jwts.builder()
            .subject(TEST_EMAIL)
            .claim("userId", TEST_USER_ID)
            .claim("nickname", TEST_NICKNAME)
            .claim("auth", TEST_ROLE)
            .issuedAt(new Date(System.currentTimeMillis() - 1000000))
            .expiration(new Date(System.currentTimeMillis() - 1000)) // 1초 전에 만료
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(expiredToken))
            .isInstanceOf(ExpiredTokenException.class)
            .hasMessageContaining("만료된 JWT 토큰");
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getEmailFromToken_Success() {
        // given
        String accessToken = jwtProvider.createAccessToken(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NICKNAME,
            TEST_ROLE
        );

        // when
        String email = jwtProvider.getEmailFromToken(accessToken);

        // then
        assertThat(email).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("토큰에서 Authentication 객체 생성 성공")
    void getAuthentication_Success() {
        // given
        String accessToken = jwtProvider.createAccessToken(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NICKNAME,
            TEST_ROLE
        );

        // when
        Authentication authentication = jwtProvider.getAuthentication(accessToken);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        // Principal 검증
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertThat(userDetails.getId()).isEqualTo(TEST_USER_ID);
        assertThat(userDetails.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(userDetails.getNickname()).isEqualTo(TEST_NICKNAME);
        assertThat(userDetails.getRole()).isEqualTo(Role.USER);

        // Credentials 검증 (토큰 자체)
        assertThat(authentication.getCredentials()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("권한 정보가 없는 토큰으로 Authentication 생성 실패")
    void getAuthentication_NoAuthority_ThrowsException() {
        // given: 권한 정보 없는 토큰
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));

        String tokenWithoutAuth = Jwts.builder()
            .subject(TEST_EMAIL)
            .claim("userId", TEST_USER_ID)
            .claim("nickname", TEST_NICKNAME)
            // auth claim 없음!
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 900000))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.getAuthentication(tokenWithoutAuth))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessageContaining("권한 정보가 없는 토큰");
    }

    @Test
    @DisplayName("Access Token 만료 시간 반환")
    void getAccessTokenExpiration_ReturnsCorrectValue() {
        // when
        Duration expiration = jwtProvider.getAccessTokenExpiration();

        // then
        assertThat(expiration).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("Refresh Token 만료 시간 반환")
    void getRefreshTokenExpiration_ReturnsCorrectValue() {
        // when
        Duration expiration = jwtProvider.getRefreshTokenExpiration();

        // then
        assertThat(expiration).isEqualTo(Duration.ofDays(7));
    }
}
