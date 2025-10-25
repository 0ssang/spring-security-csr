package com.study.jwtauth.infrastructure.security.oidc;

import java.util.Map;

/**
 * Naver OIDC 사용자 정보
 * CustomOidcUserService에서 이미 ID Token의 sub와 UserInfo의 데이터를 병합한
 * OIDC 표준 형식 attributes를 받음:
 * {
 *   "sub": "naver_user_id",         // ID Token의 sub
 *   "email": "user@example.com",    // UserInfo의 response.email
 *   "name": "홍길동",                // UserInfo의 response.name
 *   "id": "..."                      // UserInfo의 response.id (참고용)
 * }
 */
public class NaverOidcUserInfo implements OidcUserInfo {

    private final Map<String, Object> attributes;

    public NaverOidcUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // OIDC 표준: sub (subject) 클레임
        // CustomOidcUserService에서 ID Token의 sub를 이미 설정함
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        // OIDC 표준: email 클레임
        // CustomOidcUserService에서 UserInfo의 response.email을 이미 설정함
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // OIDC 표준: name 클레임
        // CustomOidcUserService에서 UserInfo의 response.name을 이미 설정함
        return (String) attributes.get("name");
    }

    @Override
    public String getImageUrl() {
        // 네이버는 프로필 이미지를 OIDC UserInfo에서 제공하지 않음
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
