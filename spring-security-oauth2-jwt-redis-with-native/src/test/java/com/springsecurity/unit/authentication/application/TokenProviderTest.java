package com.springsecurity.unit.authentication.application;


import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.springsecurity.support.UserGivenHelper.createAuthentication;
import static com.springsecurity.support.UserGivenHelper.givenGoogleUser;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenProviderTest {

    TokenProvider tokenProvider;

    @BeforeEach
    void setup(){
        tokenProvider = new TokenProvider("secret",3600000L,86400000L);
    }

    @Test
    void accessToken_만들기() throws Exception{
        //given
        User user = givenGoogleUser();

        //when
        String accessToken = getAccessToken(user);

        //then
        String userEmailFromToken = tokenProvider.getUserEmailFromToken(accessToken);
        assertThat(userEmailFromToken).isEqualTo(user.getEmail());
    }

    @Test
    void refreshToken_만들기() throws Exception{
        //given
        User user = givenGoogleUser();

        //when
        String refreshToken = tokenProvider.createRefreshToken(createAuthentication(user));

        //then
        String userEmailFromToken = tokenProvider.getUserEmailFromToken(refreshToken);
        assertThat(userEmailFromToken).isEqualTo(user.getEmail());
    }

    @Test
    void 토큰에서_이메일_추출하기() throws Exception{
        //given
        User user = givenGoogleUser();
        String accessToken = getAccessToken(user);

        //when
        String userEmailFromToken = tokenProvider.getUserEmailFromToken(accessToken);

        //then
        assertThat(userEmailFromToken).isEqualTo(user.getEmail());
    }

    @Test
    void 토큰_남은_시간_추출하기() throws Exception{
        //given
        String accessToken = getAccessToken(givenGoogleUser());

        //when
        long remainingMilliSecondsFromToken = tokenProvider.getRemainingMilliSecondsFromToken(accessToken);

        //then
        assertThat(remainingMilliSecondsFromToken).isGreaterThan(3599000L);
        assertThat(remainingMilliSecondsFromToken).isLessThan(3600001L);
    }

    @Test
    void 토큰_유효성_체크_성공() throws Exception{
        //given
        String accessToken = getAccessToken(givenGoogleUser());

        //when
        boolean result = tokenProvider.validateToken(accessToken);

        // then
        assertThat(result).isTrue();
    }



    @Test
    void secret이_다른_토큰_유효성_체크() throws Exception{
        //given
        String accessToken = getAccessToken(givenGoogleUser());
        TokenProvider anotherTokenProvider = new TokenProvider("new_secret", 10000L, 10000L);

        //when
        boolean result = anotherTokenProvider.validateToken(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 구조적_문제가_있는_토큰_유효성_체크() throws Exception{
        //given
        String accessToken = "dummy";

        //when
        boolean result = tokenProvider.validateToken(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 만료된_토큰_유효성_체크() throws Exception{
        //given
        tokenProvider = new TokenProvider("secret", 0L, 0L);
        String accessToken = tokenProvider.createAccessToken(createAuthentication(givenGoogleUser()));

        //when
        boolean result = tokenProvider.validateToken(accessToken);

        // then
        assertThat(result).isFalse();
    }

    private String getAccessToken(User user) {
        return tokenProvider.createAccessToken(createAuthentication(user));
    }

}
