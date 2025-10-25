package com.study.jwtauth.infrastructure.security.oidc;

import java.util.Map;

/**
 * Google OIDC 사용자 정보
 * OIDC standard claims 사용
 * - sub: 사용자 고유 ID
 * - email: 이메일
 * - name: 이름
 * - picture: 프로필 이미지
 */
public class GoogleOidcUserInfo implements OidcUserInfo {

    private final Map<String, Object> attributes;

    public GoogleOidcUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
