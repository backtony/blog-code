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

                // session ????????????
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // form login ??????
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper)) // ?????? ??????
                .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)) // ?????? ??????

                .and()
                .oauth2Login()
                .successHandler(loginSuccessHandler)
                .userInfoEndpoint()
                .userService(customOauth2Service)

                .and()
                .authorizationEndpoint()
                // /oauth2/authorization/google ?????? naver??? ?????????
                // oauth ?????? url -> /api/oauth2/authorization/naver ?????? google
                .baseUri("/api/oauth2/authorization")
        ;

        http.addFilterBefore(tokenAuthenticationErrorFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    // todo ????????? ????????? ??????
    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter(objectMapper);
        loginFilter.setFilterProcessesUrl("/api/v1/login"); // login ?????????
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(new LoginFailureHandler(objectMapper)); // ?????? ????????? ?????? ?????? ??????
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
