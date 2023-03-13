package com.oauth2.albumresourceserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .authorizeHttpRequests()
//            .anyRequest()
//            .permitAll()
            .requestMatchers("/albums/**")
            .hasAnyAuthority("SCOPE_album.read", "SCOPE_album.write")
            .and()
            // jwt 토큰을 검증하는 빈들과 클래스들을 생성하고 초기화시킨다.
            // same as -> http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
            .oauth2ResourceServer().jwt()

        return http.build()
    }
}
