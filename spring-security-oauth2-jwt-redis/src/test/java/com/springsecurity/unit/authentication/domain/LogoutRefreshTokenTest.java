package com.springsecurity.unit.authentication.domain;

import com.springsecurity.authentication.domain.LogoutAccessToken;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogoutRefreshTokenTest {

    @Test
    void LogoutRefreshToken_정적_메서드_생성() throws Exception{
        //given
        String refreshToken = "refreshToken";
        long expiration = 86400000;

        //when
        LogoutAccessToken logoutAccessToken = LogoutAccessToken.of(refreshToken, expiration);

        //then
        assertThat(logoutAccessToken.getId()).isEqualTo(refreshToken);
        assertThat(logoutAccessToken.getExpiration()).isEqualTo(expiration);
    }
}
