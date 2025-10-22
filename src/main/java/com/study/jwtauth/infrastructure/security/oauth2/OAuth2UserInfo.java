package com.study.jwtauth.infrastructure.security.oauth2;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보를 추상화하는 인터페이스
 * Google(OIDC), Kakao, Naver의 서로 다른 attributes 구조를 통일된 방식으로 접근
 */
public interface OAuth2UserInfo {

    /**
     * OAuth2 제공자에서 제공하는 사용자 고유 ID
     * - Google: sub (subject)
     * - Kakao: id
     * - Naver: id
     */
    String getProviderId();

    /**
     * OAuth2 제공자 이름 (google, kakao, naver)
     */
    String getProvider();

    /**
     * 사용자 이메일
     */
    String getEmail();

    /**
     * 사용자 이름/닉네임
     */
    String getName();

    /**
     * 프로필 이미지 URL (선택적)
     */
    String getImageUrl();

    /**
     * 원본 attributes (디버깅용)
     */
    Map<String, Object> getAttributes();
}
