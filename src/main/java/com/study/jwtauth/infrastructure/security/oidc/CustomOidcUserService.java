package com.study.jwtauth.infrastructure.security.oidc;

import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Set;

/**
 * OIDC 사용자 정보를 로드하는 서비스 (Google, Kakao, Naver)
 * OAuth2 방식을 OIDC로 통합
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // registrationId 추출 (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 네이버 OIDC 특수 처리: UserInfo가 OAuth2 방식(response wrapping)을 사용하므로
        // super.loadUser()를 우회하고 직접 처리
        if ("naver".equals(registrationId)) {
            return loadNaverOidcUser(userRequest);
        }

        // 구글, 카카오: 표준 OIDC 흐름
        OidcUser oidcUser = super.loadUser(userRequest);

        // OidcUserInfo로 사용자 정보 추상화 (Factory 패턴 활용)
        OidcUserInfo oidcUserInfo = OidcUserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oidcUser.getAttributes()
        );

        // 사용자 저장 또는 업데이트
        saveOrUpdate(oidcUserInfo);

        // OidcUser 반환 (SuccessHandler로 전달됨)
        return oidcUser;
    }

    /**
     * OIDC 사용자 저장 또는 업데이트
     * - 신규 사용자: User + UserProvider 생성
     * - 기존 사용자 (같은 provider): 정보 업데이트
     * - 기존 사용자 (다른 provider): 새 provider 추가
     */
    private User saveOrUpdate(OidcUserInfo oidcUserInfo) {
        String provider = oidcUserInfo.getProvider();
        String providerId = oidcUserInfo.getProviderId();
        String email = oidcUserInfo.getEmail();
        String name = oidcUserInfo.getName();

        // provider + providerId로 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(user -> {
                    // 기존 사용자: 정보 업데이트
                    log.info("기존 OIDC 사용자 업데이트: userId={}, provider={}", user.getId(), provider);
                    user.updateOidcInfo(name);
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
                                User newUser = User.createOidcUser(email, name, provider, providerId);
                                return userRepository.save(newUser);
                            });
                });
    }

    /**
     * 네이버 OIDC 사용자 정보 로드 (특수 처리)
     * 네이버는 ID Token은 OIDC 표준이지만, UserInfo는 OAuth2 방식(response wrapping)을 사용
     * 따라서 ID Token의 sub와 UserInfo의 데이터를 수동으로 병합
     */
    private OidcUser loadNaverOidcUser(OidcUserRequest userRequest) {
        // 1. ID Token 추출 (OIDC 표준)
        OidcIdToken idToken = userRequest.getIdToken();
        String sub = idToken.getSubject();  // sub 클레임

        // 2. UserInfo 호출 (OAuth2 방식 응답)
        Map<String, Object> userInfoResponse = fetchNaverUserInfo(userRequest);
        Map<String, Object> response = (Map<String, Object>) userInfoResponse.get("response");

        // 3. OIDC 표준 형식으로 attributes 생성 (ID Token sub + UserInfo 데이터 병합)
        Map<String, Object> attributes = new java.util.HashMap<>();
        attributes.put("sub", sub);                          // ID Token의 sub 사용
        attributes.put("email", response.get("email"));      // UserInfo의 email
        attributes.put("name", response.get("name"));        // UserInfo의 name
        attributes.put("id", response.get("id"));            // 네이버 고유 ID (참고용)

        // 4. OidcUserInfo로 사용자 정보 추상화
        OidcUserInfo oidcUserInfo = OidcUserInfoFactory.getOAuth2UserInfo(
                "naver",
                attributes  // 병합된 표준 형식 attributes
        );

        // 5. 사용자 저장 또는 업데이트
        saveOrUpdate(oidcUserInfo);

        // 6. DefaultOidcUser 생성 및 반환
        Set<org.springframework.security.core.GrantedAuthority> authorities =
            Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));

        // OidcUserInfo 생성 (attributes를 UserInfo로 변환)
        org.springframework.security.oauth2.core.oidc.OidcUserInfo userInfo =
                new org.springframework.security.oauth2.core.oidc.OidcUserInfo(attributes);

        return new org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser(
                authorities,
                idToken,
                userInfo,
                "sub"  // userNameAttributeName
        );
    }

    /**
     * 네이버 UserInfo 엔드포인트 호출
     * OAuth2 방식의 response wrapping된 응답을 반환
     */
    private Map<String, Object> fetchNaverUserInfo(OidcUserRequest userRequest) {
        try {
            String userInfoUri = userRequest.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUri();
            String accessToken = userRequest.getAccessToken().getTokenValue();

            RestClient restClient = RestClient.create();
            return restClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (Exception e) {
            log.error("네이버 UserInfo 호출 실패", e);
            throw new OAuth2AuthenticationException("네이버 UserInfo 호출 실패");
        }
    }
}
