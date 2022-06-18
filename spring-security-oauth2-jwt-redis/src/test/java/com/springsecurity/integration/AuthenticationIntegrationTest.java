package com.springsecurity.integration;

import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.support.IntegrationTest;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.springsecurity.support.UserGivenHelper.createAuthentication;
import static com.springsecurity.support.UserGivenHelper.givenUser;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationIntegrationTest extends IntegrationTest {

    private final String URL = "/api/v1/";
    private final String REFRESH_HEADER = "refreshToken";
    private final String TOKEN_TYPE = "Bearer ";


    @Autowired UserRepository userRepository;
    @Autowired TokenProvider tokenProvider;


    @Test
    void 로그아웃() throws Exception{

        //given
        User user = userRepository.save(givenUser());
        Authentication authentication = createAuthentication(user);
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
        User user = userRepository.save(givenUser());
        Authentication authentication = createAuthentication(user);
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


}
