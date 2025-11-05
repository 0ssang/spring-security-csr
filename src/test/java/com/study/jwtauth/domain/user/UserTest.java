package com.study.jwtauth.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

/**
 * User 도메인 단위 테스트
 *
 * 테스트 범위:
 * - 사용자 생성 (일반, 관리자, OAuth2)
 * - 닉네임 업데이트
 * - Provider 추가
 * - 이메일/닉네임 검증
 * - equals/hashCode
 */
@DisplayName("User 도메인 테스트")
class UserTest {

    private PasswordEncoder passwordEncoder;

    /**
     * 각 테스트 실행 전 PasswordEncoder 초기화
     * 실제 BCryptPasswordEncoder를 사용 (Mock 아님)
     */
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // ========================================
    // createUser() 테스트
    // ========================================

    @Test
    @DisplayName("일반 사용자 생성 성공")
    void createUser_Success() {
        // given (준비): 테스트에 필요한 데이터
        String email = "test@example.com";
        String rawPassword = "password123";
        String nickname = "테스터";

        // when (실행): 실제 테스트할 메서드 실행
        User user = User.createUser(email, rawPassword, nickname, passwordEncoder);

        // then (검증): 결과가 예상대로인지 확인
        // 1. User 기본 정보 검증
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRole()).isEqualTo(Role.USER);

        // 2. UserProvider 검증
        assertThat(user.getProviders()).hasSize(1);

        UserProvider provider = user.getProviders().stream()
                .findFirst()
                .orElseThrow();

        assertThat(provider.getProvider()).isEqualTo("local");

        // 3. 비밀번호 암호화 검증
        assertThat(provider.getPassword()).isNotNull();
        assertThat(provider.getPassword()).isNotEqualTo(rawPassword); // 암호화되어야 함
        assertThat(passwordEncoder.matches(rawPassword, provider.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이메일이 null일 때 예외 발생")
    void createUser_EmailNull_ThrowsException() {
        // given
        String email = null;
        String rawPassword = "password123";
        String nickname = "테스터";

        // when & then
        // assertThatThrownBy: 예외가 발생하는지 검증
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)  // 예외 타입 검증
            .hasMessageContaining("이메일은 필수입니다");   // 예외 메시지 검증
    }

    @Test
    @DisplayName("이메일이 빈 문자열일 때 예외 발생")
    void createUser_EmailEmpty_ThrowsException() {
        // given
        String email = "";
        String rawPassword = "password123";
        String nickname = "테스터";

        // when & then
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일 형식이 잘못되었을 때 예외 발생")
    void createUser_EmailInvalidFormat_ThrowsException() {
        // given
        String email = "invalid-email";  // @ 없음
        String rawPassword = "password123";
        String nickname = "테스터";

        // when & then
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("닉네임이 null일 때 예외 발생")
    void createUser_NicknameNull_ThrowsException() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String nickname = null;

        // when & then
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("닉네임은 필수입니다");
    }

    @Test
    @DisplayName("닉네임이 1자일 때 예외 발생 (최소 2자)")
    void createUser_NicknameTooShort_ThrowsException() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String nickname = "a";  // 1자

        // when & then
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("닉네임은 2-20자 사이여야 합니다");
    }

    @Test
    @DisplayName("닉네임이 21자일 때 예외 발생 (최대 20자)")
    void createUser_NicknameTooLong_ThrowsException() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String nickname = "123456789012345678901";  // 21자

        // when & then
        assertThatThrownBy(() ->
            User.createUser(email, rawPassword, nickname, passwordEncoder)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("닉네임은 2-20자 사이여야 합니다");
    }

    // TODO: createAdmin() 테스트 작성
    // - createAdmin_Success()
    // - createAdmin_InvalidEmail_ThrowsException()

    // TODO: createOAuth2User() 테스트 작성
    // - createOAuth2User_Google_Success()
    // - createOAuth2User_Kakao_Success()
    // - createOAuth2User_Naver_Success()

    // TODO: updateOAuth2Info() 테스트 작성
    // - updateOAuth2Info_Success()
    // - updateOAuth2Info_NullNickname_Ignored()
    // - updateOAuth2Info_EmptyNickname_Ignored()
    // - updateOAuth2Info_InvalidNickname_ThrowsException()

    // TODO: addProvider() 테스트 작성
    // - addProvider_Success()
    // - addProvider_MultipleProviders_Success()

    // TODO: equals() 및 hashCode() 테스트 작성
    // - equals_SameId_ReturnsTrue()
    // - equals_DifferentId_ReturnsFalse()
    // - equals_SameObject_ReturnsTrue()
    // - equals_Null_ReturnsFalse()

    /*

  1. createAdmin() - 2개

  @Test
  @DisplayName("관리자 생성 성공")
  void createAdmin_Success() {
      // Hint: createUser_Success()와 거의 동일
      // 차이점: Role.ADMIN인지 확인
  }

  2. createOAuth2User() - 3개

  @Test
  @DisplayName("OAuth2 사용자 생성 성공 - Google")
  void createOAuth2User_Google_Success() {
      // Hint: PasswordEncoder 불필요
      // provider = "google", providerId = "google-123" 등
      // UserProvider의 provider 필드 확인
  }

  3. updateOAuth2Info() - 4개

  @Test
  @DisplayName("OAuth2 닉네임 업데이트 성공")
  void updateOAuth2Info_Success() {
      // 1. OAuth2 사용자 생성
      // 2. updateOAuth2Info("새닉네임") 호출
      // 3. 닉네임이 변경되었는지 확인
  }

  @Test
  @DisplayName("null 닉네임으로 업데이트 시도 - 무시됨")
  void updateOAuth2Info_NullNickname_Ignored() {
      // Hint: 닉네임이 변경되지 않아야 함
  }

  4. addProvider() - 2개

  @Test
  @DisplayName("기존 사용자에 새 Provider 추가 성공")
  void addProvider_Success() {
      // 1. local 사용자 생성
      // 2. addProvider("google", "google-123")
      // 3. providers.size() == 2 확인
  }

  5. equals/hashCode - 4개

  @Test
  @DisplayName("같은 ID를 가진 User는 동등하다")
  void equals_SameId_ReturnsTrue() {
      // Hint: Reflection으로 id 설정 필요
      // 또는 동일 객체 비교
  }

     */
}