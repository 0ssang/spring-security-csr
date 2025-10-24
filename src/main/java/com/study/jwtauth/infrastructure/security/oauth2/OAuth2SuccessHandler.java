package com.study.jwtauth.infrastructure.security.oauth2;

import com.study.jwtauth.domain.auth.RefreshToken;
import com.study.jwtauth.domain.auth.RefreshTokenRepository;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.domain.user.exception.UserNotFoundException;
import com.study.jwtauth.infrastructure.logging.StructuredLogger;
import com.study.jwtauth.infrastructure.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    private static final Logger authLogger = LoggerFactory.getLogger("AUTH_LOGGER");

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();

        // 1. registrationId 추출 (google, kakao, naver)
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // 2. OAuth2UserInfo를 통해 email 추출
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oAuth2User.getAttributes()
        );
        String email = oAuth2UserInfo.getEmail();

        // 3. email로 DB에서 User 조회
        User user = userRepository.findByEmailWithProvider(email)
                .orElseThrow(UserNotFoundException::new);

        // 4. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // 5. Refresh Token을 Redis에 저장
        refreshTokenRepository.save(RefreshToken.of(
                user.getEmail(),
                refreshToken,
                jwtProvider.getRefreshTokenExpiration()
        ));

        // 6. 구조화된 로그로 OAuth2 로그인 성공 기록
        StructuredLogger.logAuthSuccess(authLogger, user.getEmail(), user.getId(), registrationId);

        // 7. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("프론트엔드로 리다이렉트: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
