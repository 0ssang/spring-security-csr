package com.study.jwtauth.domain.user;

import com.study.jwtauth.domain.user.exception.InvalidEmailFormatException;
import com.study.jwtauth.domain.user.exception.InvalidNicknameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 테스트")
class UserTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Nested
    @DisplayName("createUser 팩토리 메서드 테스트")
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
                    .isInstanceOf(InvalidEmailFormatException.class)
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
                    .isInstanceOf(InvalidEmailFormatException.class)
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
                    .isInstanceOf(InvalidEmailFormatException.class)
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
                    .isInstanceOf(InvalidEmailFormatException.class)
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
                    .isInstanceOf(InvalidEmailFormatException.class)
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
                    .isInstanceOf(InvalidNicknameException.class)
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
                    .isInstanceOf(InvalidNicknameException.class)
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
                    .isInstanceOf(InvalidNicknameException.class)
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
                    .isInstanceOf(InvalidNicknameException.class)
                    .hasMessageContaining("닉네임은 2-20자 사이여야 합니다");
        }
    }

    @Nested
    @DisplayName("createAdmin 팩토리 메서드 테스트")
    class CreateAdmin {

        @Test
        @DisplayName("유효한 정보로 관리자를 생성한다")
        void createAdmin_withValidInfo_Success(){
            // given
            String email = "admin@example.com";
            String rawPassword = "password123";
            String nickname = "admin";

            // when
            User user = User.createAdmin(email, rawPassword, nickname, passwordEncoder);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("생성 시 관리자의 local provider가 자동으로 추가된다")
        void createAdmin_AutomaticallyAddsLocalProvider() {
            // given
            String email = "admin@example.com";
            String rawPassword = "password123";
            String nickname = "admin";

            // when
            User user = User.createAdmin(email, rawPassword, nickname, passwordEncoder);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            assertThat(user.getProviders()).hasSize(1);

            UserProvider provider = user.getProviders().iterator().next();

            assertThat(provider.getProvider()).isEqualTo("local");
            assertThat(provider.getProviderId()).isNull();
            assertThat(provider.getPassword()).isNotNull();
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 생성 시 예외가 발생한다.")
        void createAdmin_WithInvalidEmail_ThrowsException() {
            // given
            String email = "wrong-type";
            String rawPassword = "password123";
            String nickname = "oidcuser";

            // when & then
            assertThatThrownBy(() -> User.createAdmin(email, nickname, rawPassword, passwordEncoder))
                .isInstanceOf(InvalidEmailFormatException.class);
        }
    }

    @Nested
    @DisplayName("createOidcUser 팩토리 메서드 테스트")
    class CreateOidcUser {

        @Test
        @DisplayName("유효한 정보로 OIDC 유저를 생성한다")
        void createOidcUser_WithValidInfo_Success(){
            // given
            String email = "user1@example.com";
            String nickname = "user1";
            String provider = "google";
            String providerId = "g01234";

            // when
            User user = User.createOidcUser(email, nickname, provider, providerId);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getRole()).isEqualTo(Role.USER);
            assertThat(user.getNickname()).isEqualTo(nickname);
            assertThat(user.getProviders()).hasSize(1);
        }

        @Test
        @DisplayName("생성 시 Provider 정보가 올바르게 추가된다")
        void createOidcUser_CorrectlyAddsProviderInfo(){
            // given
            String email = "user1@example.com";
            String nickname = "user1";
            String provider = "google";
            String providerId = "g01234";

            // when
            User user = User.createOidcUser(email, nickname, provider, providerId);

            // then
            assertThat(user.getProviders()).hasSize(1);
            UserProvider createdProvider = user.getProviders().iterator().next();

            assertThat(createdProvider.getPassword()).isNull();
            assertThat(createdProvider.getProvider()).isEqualTo(provider);
            assertThat(createdProvider.getProviderId()).isEqualTo(providerId);
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 생성 시 예외가 발생한다.")
        void createOidcUser_WithInvalidEmail_ThrowsException() {
            // given
            String email = "wrong-type";
            String nickname = "user1";
            String provider = "google";
            String providerId = "g01234";

            // when & then
            assertThatThrownBy(() -> User.createOidcUser(email, nickname, provider, providerId))
                    .isInstanceOf(InvalidEmailFormatException.class);
        }
    }

    @Nested
    @DisplayName("updateOidcInfo 메서드 테스트")
    class UpdateOidcInfo {

        @Test
        @DisplayName("유효한 닉네임으로 oidc 유저의 닉네임을 업데이트한다")
        void updateOidcInfo_WithValidNickname_Success(){
            // given
            String email = "test@test.com";
            String nickname = "test";
            String provider = "google";
            String providerId = "g01234";
            User user = User.createOidcUser(email, nickname, provider, providerId);

            String newNickname = "newNickname";

            // when
            user.updateOidcInfo(newNickname);

            // then
            assertThat(user.getNickname()).isEqualTo(newNickname);
        }

        @Test
        @DisplayName("유효하지 않은 닉네임으로 업데이트를 시도하는 경우 예외를 발생한다")
        void updateOidcInfo_WithInvalidNickname_ThrowsException() {
            // given
            String email = "test@test.com";
            String nickname = "test";
            String provider = "google";
            String providerId = "g01234";
            User user = User.createOidcUser(email, nickname, provider, providerId);

            String invalidNickname = "1";

            // when & then
            assertThatThrownBy(() -> user.updateOidcInfo(invalidNickname))
                    .isInstanceOf(InvalidNicknameException.class);
        }
    }

    @Nested
    @DisplayName("addProvider 메서드 테스트")
    class AddProvider {

        @Test
        @DisplayName("새로운 provider 추가에 성공한다")
        void addProvider_Success(){
            // given
            String email = "test@test.com";
            String password = "password123";
            String nickname = "test";

            User user = User.createUser(email, password, nickname, passwordEncoder);

            // when
            String newProvider = "kakao";
            String newProviderId = "k1234";
            user.addProvider(newProvider, newProviderId);

            // then
            assertThat(user.getProviders()).hasSize(2);
            assertThat(user.getProviders())
                    .extracting(UserProvider::getProvider, UserProvider::getProviderId)
                    .contains(
                            tuple("local", null),
                            tuple(newProvider, newProviderId)
                    );
        }
    }

    @Nested
    @DisplayName("equals & hashcode 메서드 테스트")
    class EqualsAndHashCode {

        @Test
        @DisplayName("자기 자신과는 항상 동일하다")
        void equals_WithSelf() {
            // given
            User user = User.createUser("test@test.com", "password123", "닉네임", passwordEncoder);

            // when & then
            assertThat(user).isEqualTo(user);
            assertThat(user.hashCode()).isEqualTo(user.hashCode());
        }

        @Test
        @DisplayName("다른 이메일을 가진 User는 다르다")
        void equals_WithDifferentEmail() {
            // given
            User user1 = User.createUser("test1@test.com", "password", "닉네임1", passwordEncoder);
            User user2 = User.createUser("test2@test.com", "password", "닉네임2", passwordEncoder);

            // when & then
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("null과는 동일하지 않다")
        void equals_WithNull() {
            // given
            User user = User.createUser("test@test.com", "password", "닉네임", passwordEncoder);

            // when & then
            assertThat(user).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 동일하지 않다")
        void equals_WithDifferentType() {
            // given
            User user = User.createUser("test@test.com", "password", "닉네임", passwordEncoder);
            String notUser = "문자열";

            // when & then
            assertThat(user).isNotEqualTo(notUser);
        }

        @Test
        @DisplayName("같은 객체를 Set에 여러 번 추가해도 하나만 유지된다")
        void hashCode_SetBehavior_SameReference() {
            // given
            User user = User.createUser("test@test.com", "password", "닉네임", passwordEncoder);

            // when
            Set<User> users = new HashSet<>();
            users.add(user);
            users.add(user);  // 같은 참조

            // then
            assertThat(users).hasSize(1);
        }

        @Test
        @DisplayName("email이 같으면 hashCode도 일관성을 보장한다")
        void hashCode_Consistency() {
            // given
            User user = User.createUser("test@test.com", "password", "닉네임", passwordEncoder);

            // when
            int hash1 = user.hashCode();
            int hash2 = user.hashCode();

            // then
            assertThat(hash1).isEqualTo(hash2);
        }
    }

}
