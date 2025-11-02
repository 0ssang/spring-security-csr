package com.study.jwtauth.infrastructure.config;

import com.study.jwtauth.infrastructure.security.oidc.CustomOidcUserService;
import com.study.jwtauth.infrastructure.security.oidc.OidcSuccessHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.mockito.Mockito.mock;

/**
 * 테스트용 Security 설정
 * OAuth2 관련 빈들을 Mock으로 제공하여 테스트 환경에서 정상 작동하도록 함
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * CustomOidcUserService Mock 빈
     * OAuth2/OIDC 로그인 테스트가 아닌 경우 실제 구현이 필요 없음
     */
    @Bean
    @Primary
    public CustomOidcUserService customOidcUserService() {
        return mock(CustomOidcUserService.class);
    }

    /**
     * OidcSuccessHandler Mock 빈
     * OAuth2/OIDC 로그인 테스트가 아닌 경우 실제 구현이 필요 없음
     */
    @Bean
    @Primary
    public OidcSuccessHandler oidcSuccessHandler() {
        return mock(OidcSuccessHandler.class);
    }

    /**
     * ClientRegistrationRepository Mock 빈
     * OAuth2 로그인 설정을 위해 필요한 더미 ClientRegistration 제공
     */
    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration dummyRegistration = ClientRegistration
                .withRegistrationId("test")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "email", "profile")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/oauth2/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://example.com/oauth2/jwks")
                .clientName("Test")
                .build();

        return new InMemoryClientRegistrationRepository(dummyRegistration);
    }
}
