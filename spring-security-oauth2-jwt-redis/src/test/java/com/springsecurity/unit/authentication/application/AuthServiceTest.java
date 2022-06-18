package com.springsecurity.unit.authentication.application;

import com.springsecurity.authentication.application.AuthService;
import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.domain.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks AuthService authService;
    @Mock TokenProvider tokenProvider;
    @Mock TokenRepository tokenRepository;

    @Test
    void 로그아웃() throws Exception{
        //given
        String accessToken = "Bearer accessToken";
        String refreshToken = "Bearer accessToken";

        //when
        authService.logout(accessToken,refreshToken);

        //then
        then(tokenRepository).should().saveLogoutAccessToken(ArgumentMatchers.any());
        then(tokenRepository).should().saveLogoutRefreshToken(ArgumentMatchers.any());
    }

    @Test
    void 토큰_재발급() throws Exception{
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createAccessToken(ArgumentMatchers.any())).willReturn(accessToken);
        given(tokenProvider.createRefreshToken(ArgumentMatchers.any())).willReturn(refreshToken);

        //when
        AuthResponseDto authResponseDto = authService.reissue(refreshToken);

        //then
        assertThat(authResponseDto.getTokenType()).isEqualTo(authResponseDto.getTokenType());
        assertThat(authResponseDto.getAccessToken()).isEqualTo(accessToken);
        assertThat(authResponseDto.getRefreshToken()).isEqualTo(refreshToken);
    }

}
