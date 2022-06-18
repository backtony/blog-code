package com.springsecurity.unit.authentication.infrastructure;

import com.springsecurity.authentication.domain.LogoutAccessToken;
import com.springsecurity.authentication.domain.LogoutRefreshToken;
import com.springsecurity.authentication.domain.TokenRepository;
import com.springsecurity.authentication.infrastructure.repository.LogoutAccessTokenRedisRepository;
import com.springsecurity.authentication.infrastructure.repository.LogoutRefreshTokenRedisRepository;
import com.springsecurity.authentication.infrastructure.repository.TokenRepositoryImpl;
import com.springsecurity.support.RedisRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;



public class TokenRepositoryImplTest extends RedisRepositoryTest {

    @Autowired LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    @Autowired LogoutRefreshTokenRedisRepository logoutRefreshTokenRedisRepository;
    TokenRepository tokenRepository;

    @BeforeEach
    void setup(){
        tokenRepository = new TokenRepositoryImpl(logoutAccessTokenRedisRepository,logoutRefreshTokenRedisRepository);
    }

    @BeforeEach
    void teardown(){
        logoutAccessTokenRedisRepository.deleteAll();
        logoutRefreshTokenRedisRepository.deleteAll();
    }

    @Test
    void LogoutAccessToken_저장() throws Exception{

        //given
        String accessToken = "accessToken";
        long expiration = 3600000L;

        //when
        tokenRepository.saveLogoutAccessToken(LogoutAccessToken.of(accessToken, expiration));

        //then
        assertThat(tokenRepository.existsLogoutAccessTokenById(accessToken)).isTrue();
    }

    @Test
    void LogoutRefreshToken_저장() throws Exception{
        //given
        String refreshToken = "refreshToken";
        long expiration = 3600000L;

        //when
        tokenRepository.saveLogoutRefreshToken(LogoutRefreshToken.of(refreshToken, expiration));


        //then
        assertThat(tokenRepository.existsLogoutRefreshTokenById(refreshToken)).isTrue();
    }

    @Test
    void accessToken이_저장되어_있는지_확인() throws Exception{

        //given
        String accessToken = "accessToken";
        long expiration = 3600000L;
        tokenRepository.saveLogoutAccessToken(LogoutAccessToken.of(accessToken, expiration));

        //when then
        assertThat(tokenRepository.existsLogoutAccessTokenById(accessToken)).isTrue();
    }

    @Test
    void refreshToken이_저장되어_있는지_확인() throws Exception{

        //given
        String refreshToken = "refreshToken";
        long expiration = 3600000L;
        tokenRepository.saveLogoutRefreshToken(LogoutRefreshToken.of(refreshToken, expiration));

        //when then
        assertThat(tokenRepository.existsLogoutRefreshTokenById(refreshToken)).isTrue();
    }
}
