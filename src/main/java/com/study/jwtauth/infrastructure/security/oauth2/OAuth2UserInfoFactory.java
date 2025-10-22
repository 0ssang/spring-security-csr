package com.study.jwtauth.infrastructure.security.oauth2;

import com.study.jwtauth.domain.exception.BusinessException;
import com.study.jwtauth.domain.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 객체를 생성하는 팩토리 클래스
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2UserInfoFactory {

    /**
     * registrationId에 따라 적절한 OAuth2UserInfo 구현체 생성
     *
     * @param registrationId OAuth2 제공자 ID (google, kakao, naver)
     * @param attributes OAuth2 사용자 정보 attributes
     * @return OAuth2UserInfo 구현체
     * @throws BusinessException 지원하지 않는 OAuth2 제공자인 경우
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        };
    }
}
