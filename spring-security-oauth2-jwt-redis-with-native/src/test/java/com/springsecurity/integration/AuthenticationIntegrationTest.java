package com.springsecurity.integration;

import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.config.security.dto.LoginRequest;
import com.springsecurity.support.IntegrationTest;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.springsecurity.support.UserGivenHelper.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationIntegrationTest extends IntegrationTest {

    private final String URL = "/api/v1/";
    private final String REFRESH_HEADER = "refreshToken";
    private final String TOKEN_TYPE = "Bearer ";


    @Autowired UserRepository userRepository;
    @Autowired TokenProvider tokenProvider;
    @Autowired PasswordEncoder passwordEncoder;


    @Test
    void 로그아웃() throws Exception{

        //given
        Authentication authentication = saveUserAndGetAuthentication();
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        //when then
        mockMvc.perform(MockMvcRequestBuilders.post(URL+"logout")
                .header(HttpHeaders.AUTHORIZATION,TOKEN_TYPE + accessToken)
                .header(REFRESH_HEADER,TOKEN_TYPE + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        ;
    }


    @Test
    void 토큰_재발급() throws Exception{
        //given
        Authentication authentication = saveUserAndGetAuthentication();
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        //when then
        mockMvc.perform(MockMvcRequestBuilders.post(URL+"reissue")
                .header(HttpHeaders.AUTHORIZATION,TOKEN_TYPE + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType",is(TOKEN_TYPE.trim())))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        ;
    }

    private Authentication saveUserAndGetAuthentication() {
        User user = userRepository.save(givenGoogleUser());
        return createAuthentication(user);
    }

    @Test
    void 서비스_고유_로그인() throws Exception{
        // given
        LoginRequest loginRequest = saveUserAndGetLoginRequest();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post(URL+"login")
                .content(createJson(loginRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType",is(TOKEN_TYPE.trim())))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        ;
    }

    @Test
    void 서비스_고유_회원가입() throws Exception{
        // given
        String password = "password";
        userRepository.save(givenLocalUser(passwordEncoder,password));
        LoginRequest loginRequest = new LoginRequest("backtony@gmail.com", password);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post(URL+"login")
                .content(createJson(loginRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        ;
    }


    private LoginRequest saveUserAndGetLoginRequest() {
        String password= "password";
        User user = userRepository.save(givenLocalUser(passwordEncoder,password));
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), password);
        return loginRequest;
    }


}
