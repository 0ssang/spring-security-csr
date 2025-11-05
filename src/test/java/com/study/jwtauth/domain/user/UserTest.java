package com.study.jwtauth.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 테스트")
class UserTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Nested
    @DisplayName("createUser 팩토리 메서드")
    class CreateUserTest {

        @Test
        @DisplayName("유효한 정보로 일반 사용자를 생성한다")
        void createUser_WithValidInfo_Success() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when
            User user = User.createUser(email, rawPassword, nickname, passwordEncoder);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getNickname()).isEqualTo(nickname);
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("생성 시 Local Provider가 자동으로 추가된다")
        void createUser_AutomaticallyAddsLocalProvider() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when
            User user = User.createUser(email, rawPassword, nickname, passwordEncoder);

            // then
            assertThat(user.getProviders()).hasSize(1);

            UserProvider provider = user.getProviders().iterator().next();
            assertThat(provider.getProvider()).isEqualTo("local");
            assertThat(provider.getProviderId()).isNull();
            assertThat(provider.getPassword()).isNotNull();
        }

        @Test
        @DisplayName("비밀번호가 암호화되어 저장된다")
        void createUser_PasswordIsEncoded() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when
            User user = User.createUser(email, rawPassword, nickname, passwordEncoder);

            // then
            UserProvider provider = user.getProviders().iterator().next();
            String encodedPassword = provider.getPassword();

            // 암호화된 비밀번호는 원본과 달라야 함
            assertThat(encodedPassword).isNotEqualTo(rawPassword);

            // BCrypt 암호화 형식인지 확인 (일반적으로 $2a$ 또는 $2b$로 시작)
            assertThat(encodedPassword).startsWith("$2");

            // 암호화된 비밀번호로 원본 비밀번호를 검증할 수 있어야 함
            assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        }

        @Test
        @DisplayName("null 이메일로 생성 시 예외가 발생한다")
        void createUser_WithNullEmail_ThrowsException() {
            // given
            String email = null;
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수입니다");
        }

        @Test
        @DisplayName("빈 이메일로 생성 시 예외가 발생한다")
        void createUser_WithEmptyEmail_ThrowsException() {
            // given
            String email = "";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수입니다");
        }

        @Test
        @DisplayName("공백만 있는 이메일로 생성 시 예외가 발생한다")
        void createUser_WithBlankEmail_ThrowsException() {
            // given
            String email = "   ";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수입니다");
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 생성 시 예외가 발생한다 - @ 없음")
        void createUser_WithInvalidEmailFormat_NoAtSign_ThrowsException() {
            // given
            String email = "userexample.com";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("올바른 이메일 형식이 아닙니다");
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 생성 시 예외가 발생한다 - 도메인 없음")
        void createUser_WithInvalidEmailFormat_NoDomain_ThrowsException() {
            // given
            String email = "user@";
            String rawPassword = "password123";
            String nickname = "테스트유저";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("올바른 이메일 형식이 아닙니다");
        }

        @Test
        @DisplayName("null 닉네임으로 생성 시 예외가 발생한다")
        void createUser_WithNullNickname_ThrowsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = null;

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 필수입니다");
        }

        @Test
        @DisplayName("빈 닉네임으로 생성 시 예외가 발생한다")
        void createUser_WithEmptyNickname_ThrowsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 필수입니다");
        }

        @Test
        @DisplayName("너무 짧은 닉네임(1자)으로 생성 시 예외가 발생한다")
        void createUser_WithTooShortNickname_ThrowsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "a";

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 2-20자 사이여야 합니다");
        }

        @Test
        @DisplayName("너무 긴 닉네임(21자)으로 생성 시 예외가 발생한다")
        void createUser_WithTooLongNickname_ThrowsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "password123";
            String nickname = "a".repeat(21); // 21자

            // when & then
            assertThatThrownBy(() -> User.createUser(email, rawPassword, nickname, passwordEncoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 2-20자 사이여야 합니다");
        }

    }
}
