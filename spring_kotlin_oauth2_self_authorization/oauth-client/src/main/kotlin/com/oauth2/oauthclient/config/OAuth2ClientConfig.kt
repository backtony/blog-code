package com.oauth2.oauthclient.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository

@Configuration
class OAuth2ClientConfig {

    // https://godekdls.github.io/Spring%20Security/oauth2/#oauth2authorizedclientmanager--oauth2authorizedclientprovider
    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {
        // oauth2client가 인증에 사용할 방식 지정
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()

        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientRepository
        )

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }

    // todo 운영환경 or test 환경에 따른 변동
    // https://godekdls.github.io/Spring%20Security/securitydatabaseschema/#224-oauth-20-client-schema
    // https://godekdls.github.io/Spring%20Security/oauth2/#oauth2authorizedclientrepository--oauth2authorizedclientservice
    // spring boot에서 yml에 설정해주면 알아서 매핑해서 인메모리 빈으로 등록해준다.
    // 만약 jdbc를 사용하고자 한다면 직접 jdbc로 만들어 빈 주입
    // 리얼에서는 인메모리 사용 금지 : https://youtu.be/-YbqW-pqt3w?t=1091
    @Bean
    fun authorizedClientService(
        jdbcTemplate: JdbcTemplate,
        clientRegistrationRepository: ClientRegistrationRepository
    ): OAuth2AuthorizedClientService {
        return JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository)
    }
}
