package com.study.jwtauth.application.service;

import com.study.jwtauth.domain.auth.RefreshToken;
import com.study.jwtauth.domain.auth.RefreshTokenRepository;
import com.study.jwtauth.domain.user.Role;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.domain.user.exception.*;
import com.study.jwtauth.infrastructure.security.exception.InvalidTokenException;
import com.study.jwtauth.infrastructure.security.jwt.JwtProvider;
import com.study.jwtauth.presentataion.dto.request.LoginRequest;
import com.study.jwtauth.presentataion.dto.request.RefreshTokenRequest;
import com.study.jwtauth.presentataion.dto.request.SignUpRequest;
import com.study.jwtauth.presentataion.dto.response.TokenResponse;
import com.study.jwtauth.presentataion.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("signUp 메서드 테스트")
    class SignUp {

        @Test
        @DisplayName("유효한 정보로 회원가입에 성공한다")
        void signUp_WithValidInfo_Success() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com",
                    "password123",
                    "테스트유저"
            );

            // Repository Mock 설정
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // PasswordEncoder Mock 설정
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword123");

            // User 저장 시 ID와 생성일시가 설정된 User 반환
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                // ReflectionTestUtils를 사용하여 private 필드 설정
                ReflectionTestUtils.setField(user, "id", 1L);
                return user;
            });

            // when
            UserResponse response = authService.signUp(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo(request.email());
            assertThat(response.nickname()).isEqualTo(request.nickname());
            assertThat(response.role()).isEqualTo(Role.USER);

            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).existsByNickname(request.nickname());
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(request.password());
        }

        @Test
        @DisplayName("이메일이 중복되면 예외가 발생한다")
        void signUp_WithDuplicateEmail_ThrowsException() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@test.com",
                    "password123",
                    "테스트닉네임"
            );

            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining(request.email());
            verify(userRepository).existsByEmail(request.email());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("닉네임이 중복되면 예외가 발생한다")
        void signUp_WithDuplicateNickname_ThrowsException() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@test.com",
                    "password123",
                    "테스트닉네임"
            );

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(DuplicateNicknameException.class)
                    .hasMessageContaining(request.nickname());
            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).existsByNickname(request.nickname());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 비빌번호면 예외를 발생한다")
        void signUp_WithInvalidPassword_ThrowsException() {
            SignUpRequest request = new SignUpRequest(
                    "test@test.com",
                    "short",
                    "테스트닉네임"
            );

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(InvalidPasswordFormatException.class);
            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).existsByNickname(request.nickname());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login 메서드 테스트")
    class Login {

        @Test
        @DisplayName("유효한 정보로 로그인에 성공한다")
        void login_WithValidInfo_Success() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password123");

            // Mock 설정을 User 생성 전에 먼저 수행
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

            User user = User.createUser(
                    request.email(),
                    request.password(),
                    "테스트 유저",
                    passwordEncoder
            );
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmailWithProvider(request.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn("accessToken");
            given(jwtProvider.createRefreshToken(anyString())).willReturn("refreshToken");
            given(jwtProvider.getRefreshTokenExpiration()).willReturn(Duration.ofDays(7));

            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("accessToken");
            assertThat(response.refreshToken()).isEqualTo("refreshToken");

            verify(userRepository).findByEmailWithProvider(request.email());
            verify(passwordEncoder).matches(anyString(), anyString());
            verify(jwtProvider).createAccessToken(1L, "test@test.com", "테스트 유저", "USER");
            verify(jwtProvider).createRefreshToken("test@test.com");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("사용자 조회 실패 시 예외를 발생한다")
        void login_UserNotFound_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password123");

            given(userRepository.findByEmailWithProvider(request.email()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("local provider가 아닌 사용자가 로그인 시 예외를 발생한다")
        void login_InvalidProvider_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password123");

            User user = User.createOidcUser(
                    request.email(),
                    "OIDC 유저",
                    "google",
                    "g1234"
            );

            given(userRepository.findByEmailWithProvider(request.email())).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("비밀번호가 올바르지 않은 경우 예외가 발생한다")
        void login_InvalidPassword_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password123");
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

            User user = User.createUser(
                    request.email(),
                    request.password(),
                    "테스트유저",
                    passwordEncoder
            );
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmailWithProvider(request.email())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("refresh 메서드 테스트")
    class Refresh {

        @Test
        @DisplayName("유효한 refresh 토큰을 사용하여 토큰 재발급에 성공한다")
        void refresh_WithValidInfo_Success() {
            // given
            String email = "test@test.com";
            String oldRefreshToken = "oldRefreshToken";
            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            User user = User.createUser(email, "password123", "테스트유저", passwordEncoder);
            ReflectionTestUtils.setField(user, "id", 1L);

            RefreshToken savedRefreshToken = RefreshToken.of(
                    email,
                    oldRefreshToken,
                    Duration.ofDays(7)
            );

            given(jwtProvider.getEmailFromToken(oldRefreshToken)).willReturn(email);
            given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.of(savedRefreshToken));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString()))
                    .willReturn(newAccessToken);
            given(jwtProvider.createRefreshToken(email)).willReturn(newRefreshToken);
            given(jwtProvider.getRefreshTokenExpiration()).willReturn(Duration.ofDays(7));
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            TokenResponse response = authService.refresh(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(newAccessToken);
            assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

            verify(jwtProvider).validateToken(oldRefreshToken);
            verify(jwtProvider).getEmailFromToken(oldRefreshToken);
            verify(refreshTokenRepository).findByEmail(email);
            verify(userRepository).findByEmail(email);
            verify(jwtProvider).createAccessToken(1L, email, "테스트유저", "USER");
            verify(jwtProvider).createRefreshToken(email);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Redis에 저장된 Refresh Token이 아닌 경우 예외를 발생한다")
        void refresh_TokenNotFound_ThrowsException() {
            // given
            String email = "test@test.com";
            String oldRefreshToken = "oldRefreshToken";
            RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);

            given(jwtProvider.getEmailFromToken(oldRefreshToken)).willReturn(email);
            given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("유효하지 않은 리프레시 토큰입니다");
            verify(jwtProvider).getEmailFromToken(oldRefreshToken);
            verify(refreshTokenRepository).findByEmail(email);
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Refresh 토큰이 일치하지 않으면 예외를 발생한다")
        void refresh_TokenNotMatch_ThrowsException() {
            // given
            String email = "test@test.com";
            String oldRefreshToken = "oldRefreshToken";
            RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);
            RefreshToken savedRefreshToken = RefreshToken.of(
                    email,
                    "differentOldRefreshToken",
                    Duration.ofDays(7)
            );

            given(jwtProvider.getEmailFromToken(oldRefreshToken)).willReturn(email);
            given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.of(savedRefreshToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("리프레시 토큰이 일치하지 않습니다");
            verify(jwtProvider).getEmailFromToken(oldRefreshToken);
            verify(refreshTokenRepository).findByEmail(email);
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("refresh 토큰의 email 정보에 해당하는 사용자가 없으면 예외를 발생한다")
        void refresh_UserNotFound_ThrowsException() {
            // given
            String email = "test@test.com";
            String oldRefreshToken = "oldRefreshToken";
            RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);
            RefreshToken savedRefreshToken = RefreshToken.of(
                    email,
                    oldRefreshToken,
                    Duration.ofDays(7)
            );

            given(jwtProvider.getEmailFromToken(oldRefreshToken)).willReturn(email);
            given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.of(savedRefreshToken));
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(UserNotFoundException.class);
            verify(jwtProvider).getEmailFromToken(oldRefreshToken);
            verify(refreshTokenRepository).findByEmail(email);
            verify(userRepository).findByEmail(email);
            verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("logout 메서드 테스트")
    class Logout {

        @Test
        @DisplayName("정상적으로 로그아웃에 성공한다")
        void logout_WithExistingUser_Success() {
            // given
            String email = "test@test.com";

            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            User user = User.createUser(email, "password123", "테스트유저", passwordEncoder);
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when
            authService.logout(email);

            // then
            verify(refreshTokenRepository).deleteById(email);
            verify(userRepository).findByEmail(email);
        }

        @Test
        @DisplayName("사용자가 존재하지 않아도 로그아웃에 성공한다")
        void logout_WithNonExistingUser_Success() {
            // given
            String email = "test@test.com";

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when
            authService.logout(email);

            // then
            verify(refreshTokenRepository).deleteById(email);
            verify(userRepository).findByEmail(email);
        }
    }
}
