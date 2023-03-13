package com.oauth2.authorizationserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Throws(Exception::class)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // apply AuthorizationServer config
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        // https://docs.spring.io/spring-authorization-server/docs/current/reference/html/configuration-model.html
        // initial oidc configure = Enable OpenID Connect 1.0
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(Customizer.withDefaults())
            .and()
                // 인증 안되어있으면 login url로 이동
            .exceptionHandling { exceptions: ExceptionHandlingConfigurer<HttpSecurity?> ->
                exceptions.authenticationEntryPoint(
                    LoginUrlAuthenticationEntryPoint("/login")
                )
            }
            // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer().jwt()

        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers(
                    "/monitor/**"
                ).permitAll()
                .anyRequest().authenticated()
        }
            // oauth2-client 로그인 화면창 제공
            .formLogin(Customizer.withDefaults())
        return http.build()
    }

    // test 유저
    @Bean
    fun userDetailsService(): UserDetailsService {
        val user1 = User.withUsername("user1").password("{noop}1234").authorities("ROLE_USER").build()
        val user2 = User.withUsername("user2").password("{noop}1234").authorities("ROLE_USER").build()
        return InMemoryUserDetailsManager(user1, user2)
    }
}
