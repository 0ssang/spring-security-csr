package com.study.jwtauth.application.service;

import com.study.jwtauth.domain.auth.RefreshToken;
import com.study.jwtauth.domain.auth.RefreshTokenRepository;
import com.study.jwtauth.domain.common.exception.ErrorCode;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserProvider;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.domain.user.exception.DuplicateEmailException;
import com.study.jwtauth.domain.user.exception.DuplicateNicknameException;
import com.study.jwtauth.domain.user.exception.InvalidCredentialsException;
import com.study.jwtauth.domain.user.exception.UserNotFoundException;
import com.study.jwtauth.infrastructure.security.exception.InvalidTokenException;
import com.study.jwtauth.infrastructure.security.jwt.JwtProvider;
import com.study.jwtauth.presentataion.dto.request.LoginRequest;
import com.study.jwtauth.presentataion.dto.request.RefreshTokenRequest;
import com.study.jwtauth.presentataion.dto.request.SignUpRequest;
import com.study.jwtauth.presentataion.dto.response.TokenResponse;
import com.study.jwtauth.presentataion.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException(request.nickname());
        }

        // User 생성 (UserProvider도 함께 생성됨)
        User user = User.createUser(
                request.email(),
                request.password(),
                request.nickname(),
                passwordEncoder
        );

        // 저장
        User savedUser = userRepository.save(user);

        log.info("회원가입 성공: email={}, nickname={}", savedUser.getEmail(), savedUser.getNickname());

        return UserResponse.from(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회 (UserProvider도 함께 조회)
        User user = userRepository.findByEmailWithProvider(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        // Local Provider 찾기
        UserProvider localProvider = user.getProviders().stream()
                .filter(provider -> "local".equals(provider.getProvider()))
                .findFirst()
                .orElseThrow(() -> new InvalidCredentialsException());

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), localProvider.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // Refresh Token을 Redis에 저장
        RefreshToken refreshTokenEntity = RefreshToken.of(
                user.getEmail(),
                refreshToken,
                jwtProvider.getRefreshTokenExpiration()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("로그인 성공: email={}", user.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        // Refresh Token 검증
        jwtProvider.validateToken(refreshToken);

        // Refresh Token에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);

        // Redis에서 저장된 Refresh Token 조회
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 리프레시 토큰입니다."));

        // 토큰 일치 여부 확인
        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new InvalidTokenException("리프레시 토큰이 일치하지 않습니다.");
        }

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        // 새로운 Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );

        // 새로운 Refresh Token 생성 (선택사항: Refresh Token도 갱신)
        String newRefreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // Redis에 새 Refresh Token 저장
        RefreshToken newRefreshTokenEntity = savedRefreshToken.updateToken(
                newRefreshToken,
                jwtProvider.getRefreshTokenExpiration()
        );
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("토큰 재발급 성공: email={}", user.getEmail());

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String email) {
        // Redis에서 Refresh Token 삭제
        refreshTokenRepository.deleteById(email);

        log.info("로그아웃 성공: email={}", email);
    }
}
