package com.springsecurity.config;


import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.authentication.domain.TokenRepository;
import com.springsecurity.user.domain.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.springsecurity.config.security")
public class SecurityTestConfig {

    @MockBean protected TokenProvider tokenProvider;
    @MockBean protected UserRepository userRepository;
    @MockBean protected TokenRepository tokenRepository;

}
