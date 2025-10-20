package com.study.jwtauth.infrastructure.security.jwt;

import com.study.jwtauth.infrastructure.security.exception.ExpiredTokenException;
import com.study.jwtauth.infrastructure.security.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = resolveToken(request);

            // 2. 토큰이 있고 유효한 경우 Authentication 객체를 SecurityContext에 저장
            if (StringUtils.hasText(token)) {
                jwtProvider.validateToken(token);
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다.", authentication.getName());
            }
        } catch (InvalidTokenException | ExpiredTokenException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            // 예외를 request attribute에 저장하여 AuthenticationEntryPoint에서 처리
            request.setAttribute("exception", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 정보 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
