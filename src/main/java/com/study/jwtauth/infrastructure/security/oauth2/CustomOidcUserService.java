package com.study.jwtauth.infrastructure.security.oauth2;

import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OIDC 사용자 정보를 로드하는 서비스 (Google)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OIDC 기본 사용자 정보 로드
        OidcUser oidcUser = super.loadUser(userRequest);

        // 2. registrationId 추출 (google)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OIDC 로그인 요청: provider={}", registrationId);

        // 3. OAuth2UserInfo로 사용자 정보 추상화
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oidcUser.getAttributes()
        );

        log.info("OIDC 사용자 정보: provider={}, providerId={}, email={}, name={}",
                oAuth2UserInfo.getProvider(),
                oAuth2UserInfo.getProviderId(),
                oAuth2UserInfo.getEmail(),
                oAuth2UserInfo.getName());

        // 4. 사용자 저장 또는 업데이트
        User user = saveOrUpdate(oAuth2UserInfo);

        log.info("OIDC 사용자 처리 완료: userId={}, email={}", user.getId(), user.getEmail());

        // 5. OidcUser 반환 (SuccessHandler로 전달됨)
        return oidcUser;
    }

    /**
     * OIDC 사용자 저장 또는 업데이트
     * - 신규 사용자: User + UserProvider 생성
     * - 기존 사용자 (같은 provider): 정보 업데이트
     * - 기존 사용자 (다른 provider): 새 provider 추가
     */
    private User saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {
        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        // provider + providerId로 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(user -> {
                    // 기존 사용자: 정보 업데이트
                    log.info("기존 OIDC 사용자 업데이트: userId={}, provider={}", user.getId(), provider);
                    user.updateOAuth2Info(name);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    // 신규 사용자 또는 email은 같지만 다른 provider인 경우
                    return userRepository.findByEmail(email)
                            .map(existingUser -> {
                                // email은 같지만 다른 provider: 새 provider 추가
                                log.info("기존 사용자에 새 provider 추가: userId={}, newProvider={}",
                                        existingUser.getId(), provider);
                                existingUser.addProvider(provider, providerId);
                                return userRepository.save(existingUser);
                            })
                            .orElseGet(() -> {
                                // 완전히 새로운 사용자
                                log.info("신규 OIDC 사용자 생성: email={}, provider={}", email, provider);
                                User newUser = User.createOAuth2User(email, name, provider, providerId);
                                return userRepository.save(newUser);
                            });
                });
    }
}
