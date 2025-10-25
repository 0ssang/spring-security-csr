package com.study.jwtauth.infrastructure.security.oidc;

import java.util.Map;

/**
 * Kakao OIDC 사용자 정보
 * OIDC 표준 클레임 사용:
 * {
 *   "sub": "kakao_user_id",
 *   "email": "user@example.com",
 *   "nickname": "홍길동",          // 표준 name 대신 nickname 사용
 *   "picture": "http://..."
 * }
 *
 * 참고: 카카오는 OIDC에서 'name' 대신 'nickname' 클레임 사용
 */
public class KakaoOidcUserInfo implements OidcUserInfo {

    private final Map<String, Object> attributes;

    public KakaoOidcUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // OIDC 표준: sub (subject) 클레임
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        // OIDC 표준: email 클레임 (중첩 구조 제거)
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // 카카오 OIDC: 표준 'name' 클레임을 제공하지 않고 'nickname'만 제공
        return (String) attributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        // OIDC 표준: picture 클레임 (중첩 구조 제거)
        return (String) attributes.get("picture");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
