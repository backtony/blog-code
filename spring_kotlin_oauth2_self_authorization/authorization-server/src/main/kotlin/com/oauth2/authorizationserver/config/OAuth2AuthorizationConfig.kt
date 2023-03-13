package com.oauth2.authorizationserver.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

@Configuration
class OAuth2AuthorizationConfig(
    private val oAuthClientProperties: OAuthClientProperties
) {

    // issuer에 대한 정보를 제공하는 역할 - 기본적으로 알아서 매핑되나 issuer의 url은 서버마다 다르기 때문에 별도 세팅 필요
    @Bean
    fun providerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().issuer("http://localhost:9000").build()
    }

    // todo 운영환경 or test 환경에 따른 변동
    // spring boot에서 yml에 설정해주면 알아서 매핑해서 인메모리 빈으로 등록해준다.
    // 만약 jdbc를 사용하고자 한다면 직접 jdbc로 만들어 빈 주입
    // 리얼에서는 인메모리 사용 금지 : https://youtu.be/-YbqW-pqt3w?t=1091
    // 리얼이라도 server to server의 경우, OAuth2TokenHeaderInterceptor에서 정의한대로라면 익명 사용자 또는 주석처리한 principal을
    // 사용하게 되면 RegistrationId와 principalName이 고정되기 때문에 하나만 쌓여서 inMemory를 사용해도 문제 없다.
    // 여기서 server to server는 client_credential 방식을 말한다. 만약 server to server 라도 authorization_code 방식을 사용한다면
    // InMemoryOAuth2AuthorizationService의 save메서드에서 initializedAuthorizations가 처음 code를 저장하면서 인메모리에 여럿이 쌓이게 되어 이경우에는 jdbc를 사용해야 한다.
    @Bean
    fun oAuth2AuthorizationService(jdbcTemplate: JdbcTemplate): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository())
    }

    // 동의 내용도 default가 inmemory -> 운영에서는 jdbc로
    @Bean
    fun oAuth2AuthorizationConsentService(jdbcTemplate: JdbcTemplate): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository())
    }

    // 등록되는 RegisteredClient 자체가 많지 않기 때문에 inmemory를 사용해도 된다.
    // album project는 friend를 읽을 수 있고, friend project는 아무것도, client는 전부 가능하도록
    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        val album = getRegisteredClient(
            oAuthClientProperties.album,
            setOf("friend.read", "friend.write")
        )
        val friend = getRegisteredClient(
            oAuthClientProperties.friend,
            emptySet()
        )
        val client = getRegisteredClientCodeType(
            oAuthClientProperties.client,
            // openId 사용을 알리고 뒤에는 oidcScopes에서 사용가능한 scope
            // OidcScopes.OPENID
            setOf("album.read", "album.write", "friend.read", "friend.write", "openid", "profile", "email"),
            "http://127.0.0.1:8080/login/oauth2/code/self-oauth"
        )

        return InMemoryRegisteredClientRepository(listOf(album, friend, client))
    }

    private fun getRegisteredClient(oAuthClient: OAuthClient, scopes: Set<String>): RegisteredClient {
        return baseRegisteredClientBuilder(oAuthClient, scopes)
            // 클라이언트가 토큰을 발급받기(권한 grant 를) 위해 사용할 수 있는 방식
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .build()
    }

    private fun getRegisteredClientCodeType(oAuthClient: OAuthClient, scopes: Set<String>, redirectUrl: String): RegisteredClient {
        return baseRegisteredClientBuilder(oAuthClient, scopes)
            // 클라이언트가 토큰을 발급받기(권한 grant 를) 위해 사용할 수 있는 방식
            // authorization_code를 추가하는 경우 redirectUrl를 명시해야 한다. -> oauth2client 프로젝트의 경우 code방식을 사용할 예정이므로 redirect도 oauth2client것으로 지정
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(redirectUrl)
            .build()
    }

    private fun baseRegisteredClientBuilder(oAuthClient: OAuthClient, scopes: Set<String>): RegisteredClient.Builder {
        return RegisteredClient.withId(oAuthClient.id)
            .clientId(oAuthClient.clientId)
            .clientSecret("{noop}${oAuthClient.clientSecret}")
            .scopes { it: MutableSet<String> ->
                it.addAll(
                    scopes
                )
            }
            .clientIdIssuedAt(Instant.now())
            .clientSecretExpiresAt(Instant.now().plusSeconds((60 * 60 * 24 * 365).toLong()))
            // 클라이언트가 토큰을 발급받기 위해 요청을 보낼 때 사용할 수 있는 방식
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            // 동의 여부를 물을 것인지 세팅
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
        // accessToken expire 시간 지정가능, 기본은 300초
        // .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(1L)).build())
    }

    // oidc의 역할을 하기 위해, userInfo 같은 path로 토큰가지고 요청이 오면 검증해야하기 때문에 필요
    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext?>?): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }

    // authorizationServer에서는 다른 서버들에게 jwk set = 암호화 정보를 제공해야 한다.
    @Bean
    @Throws(NoSuchAlgorithmException::class)
    fun jwkSource(): JWKSource<SecurityContext?> {
        val rsaKey = generateRsa()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    // todo instance를 여러개 띄우게 된다면 만들어진 keyPair을 공유할 수 있도록 yml에 암호화해서 넣어두고 주입받는 형식으로 사용해야 한다.
    @Throws(NoSuchAlgorithmException::class)
    private fun generateRsa(): RSAKey {
        val keyPair = generateKeyPair()
        val rsaPrivateKey = keyPair.private as RSAPrivateKey
        val rsaPublicKey = keyPair.public as RSAPublicKey
        return RSAKey.Builder(rsaPublicKey)
            .privateKey(rsaPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }
}
