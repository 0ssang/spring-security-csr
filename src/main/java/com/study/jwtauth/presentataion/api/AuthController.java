package com.study.jwtauth.presentataion.api;

import com.study.jwtauth.application.service.AuthService;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.request.LoginRequest;
import com.study.jwtauth.presentataion.dto.request.RefreshTokenRequest;
import com.study.jwtauth.presentataion.dto.request.SignUpRequest;
import com.study.jwtauth.presentataion.dto.response.TokenResponse;
import com.study.jwtauth.presentataion.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignUpRequest request) {
        log.info("회원가입 요청: email={}", request.email());

        UserResponse response = authService.signUp(request);

        return ApiResponse.created(response);
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: email={}", request.email());

        TokenResponse response = authService.login(request);

        return ApiResponse.ok(response);
    }

    /**
     * 토큰 재발급
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 재발급 요청");

        TokenResponse response = authService.refresh(request);

        return ApiResponse.ok(response);
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("로그아웃 요청: email={}", userDetails.getEmail());

        authService.logout(userDetails.getEmail());

        return ApiResponse.ok(null);
    }
}
