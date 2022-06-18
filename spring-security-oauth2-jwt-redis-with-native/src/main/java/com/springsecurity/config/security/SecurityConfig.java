package com.springsecurity.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.config.security.filter.LoginFilter;
import com.springsecurity.config.security.filter.TokenAuthenticationErrorFilter;
import com.springsecurity.config.security.filter.TokenAuthenticationFilter;
import com.springsecurity.config.security.handler.LoginFailureHandler;
import com.springsecurity.config.security.handler.LoginSuccessHandler;
import com.springsecurity.config.security.handler.RestAccessDeniedHandler;
import com.springsecurity.config.security.handler.RestAuthenticationEntryPoint;
import com.springsecurity.config.security.service.CustomOauth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOauth2Service customOauth2Service;
    private final LoginSuccessHandler loginSuccessHandler;
    private final TokenAuthenticationErrorFilter tokenAuthenticationErrorFilter;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().sameOrigin() // for h2

                // session 사용안함
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // form login 제거
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper)) // 인증 실패
                .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)) // 인가 실패

                .and()
                .oauth2Login()
                .successHandler(loginSuccessHandler)
                .userInfoEndpoint()
                .userService(customOauth2Service)

                .and()
                .authorizationEndpoint()
                // /oauth2/authorization/google 또는 naver가 기본값
                // oauth 요청 url -> /api/oauth2/authorization/naver 또는 google
                .baseUri("/api/oauth2/authorization")
        ;

        http.addFilterBefore(tokenAuthenticationErrorFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    // todo 문서에 진입점 명시
    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter(objectMapper);
        loginFilter.setFilterProcessesUrl("/api/v1/login"); // login 진입점
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(new LoginFailureHandler(objectMapper)); // 필터 안에서 예외 터질 경우
        return loginFilter;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
