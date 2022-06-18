package com.springsecurity.unit.authentication.presentation.dto;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.presentation.dto.AuthResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthResponseTest {

    @Test
    void authResponseDto_from_정적_메서드로_생성() throws Exception{
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        AuthResponseDto authResponseDto = AuthResponseDto.of(accessToken, refreshToken);

        //when
        AuthResponse authResponse = AuthResponse.from(authResponseDto);

        //then
        assertAuthResponse(accessToken, refreshToken, authResponse);
    }


    @Test
    void authResponseDto_of_정적_메서드로_생성() throws Exception{
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        //when
        AuthResponse authResponse = AuthResponse.of(accessToken,refreshToken);

        //then
        assertAuthResponse(accessToken, refreshToken, authResponse);
    }

    private void assertAuthResponse(String accessToken, String refreshToken, AuthResponse authResponse) {
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getAccessToken()).isEqualTo(accessToken);
        assertThat(authResponse.getRefreshToken()).isEqualTo(refreshToken);
    }
}
