package com.oauth2.oauthclient.config

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
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests()
            .requestMatchers("/")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .oauth2Login { authLogin ->
                authLogin.defaultSuccessUrl(
                    "/"
                )
            }

        return http.build()
    }
}
