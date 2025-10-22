package com.study.jwtauth.infrastructure.security.oauth2;

import com.study.jwtauth.domain.auth.RefreshToken;
import com.study.jwtauth.domain.auth.RefreshTokenRepository;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.domain.user.exception.UserNotFoundException;
import com.study.jwtauth.infrastructure.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2/OIDC 로그인 성공 핸들러
 * JWT 토큰을 발급하고 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. OAuth2User에서 email 추출
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 로그인 성공: email={}", email);

        // 2. email로 DB에서 User 조회
        User user = userRepository.findByEmailWithProvider(email)
                .orElseThrow(UserNotFoundException::new);

        // 3. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // 4. Refresh Token을 Redis에 저장
        refreshTokenRepository.save(RefreshToken.of(
                user.getEmail(),
                refreshToken,
                jwtProvider.getRefreshTokenExpiration()
        ));

        log.info("JWT 토큰 발급 완료: userId={}, email={}", user.getId(), user.getEmail());

        // 5. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("프론트엔드로 리다이렉트: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
