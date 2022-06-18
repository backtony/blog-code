package com.springsecurity.unit.authentication.application.dto;


import com.springsecurity.authentication.application.dto.AuthResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseDtoTest {

    @Test
    void authResponseDto_from_정적_메서드로_생성() throws Exception{
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        //when
        AuthResponseDto authResponseDto = AuthResponseDto.of(accessToken, refreshToken);

        //then
        assertThat(authResponseDto.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponseDto.getAccessToken()).isEqualTo(accessToken);
        assertThat(authResponseDto.getRefreshToken()).isEqualTo(refreshToken);
    }


}