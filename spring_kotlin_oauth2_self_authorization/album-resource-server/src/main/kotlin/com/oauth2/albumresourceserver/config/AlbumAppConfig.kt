package com.oauth2.albumresourceserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.CollectionUtils
import org.springframework.web.client.RestTemplate

@Configuration
class AlbumAppConfig(
    private val oAuth2TokenHeaderInterceptor: OAuth2TokenHeaderInterceptor
) {

    // https://recordsoflife.tistory.com/36
    // https://www.baeldung.com/spring-rest-template-interceptor
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        var interceptors = restTemplate.interceptors
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = ArrayList()
        }
        interceptors.add(oAuth2TokenHeaderInterceptor)
        restTemplate.interceptors = interceptors
        return restTemplate
    }
}
