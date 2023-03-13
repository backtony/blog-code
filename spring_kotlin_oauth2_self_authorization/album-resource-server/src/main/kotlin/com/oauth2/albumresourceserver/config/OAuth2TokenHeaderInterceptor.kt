package com.oauth2.albumresourceserver.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component
import java.util.*

// https://recordsoflife.tistory.com/36
// https://www.baeldung.com/spring-rest-template-interceptor

@Component
class OAuth2TokenHeaderInterceptor(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val manager: OAuth2AuthorizedClientManager

) : ClientHttpRequestInterceptor {

//    private fun createPrincipal(): Authentication {
//        return object : Authentication {
//            override fun getName(): String {
//                return "album-server"
//            }
//
//            override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
//                return Collections.emptySet()
//            }
//
//            override fun getCredentials(): Any? {
//                return null
//            }
//
//            override fun getDetails(): Any? {
//                return null
//            }
//
//            override fun getPrincipal(): Any {
//                return this
//            }
//
//            override fun isAuthenticated(): Boolean {
//                return false
//            }
//
//            override fun setAuthenticated(isAuthenticated: Boolean) {
//            }
//        }
//    }

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {

        // 익명 객체 또는 사용자
        // server to server 의 경우에는 principal을 컨텍스트에서 꺼내오지 않고 위의 주석 코드처럼 하나 만들어서 넣어줘도 될듯하다.
        // 컨텍스트에서 꺼내주면 사용자일때는 사용자의 principal로 요청을 하게 되니 추적상으로는 도움이 될 수도 있을 것 같다.
        val authentication: Authentication = SecurityContextHolder.getContext().getAuthentication()
        val clientRegistration = clientRegistrationRepository.findByRegistrationId(SELF_OAUTH_REGISTRATION_ID)
        val authorizeRequest: OAuth2AuthorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId(clientRegistration.registrationId)
            .principal(authentication)
            .build()

        // 인가받은 client - 인증은 안되어있음
        val oAuth2AuthorizedClient = manager.authorize(authorizeRequest)
        check(!Objects.isNull(oAuth2AuthorizedClient)) { "client credentials flow on " + clientRegistration.registrationId + " failed, client is null" }
        val accessToken = oAuth2AuthorizedClient!!.accessToken.tokenValue
        request.headers.add("Authorization", "Bearer $accessToken")
        return execution.execute(request, body)
    }

    companion object {
        private const val SELF_OAUTH_REGISTRATION_ID = "self-oauth"
    }
}
