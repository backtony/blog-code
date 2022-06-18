package com.springsecurity.authentication.presentation.dto;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class AuthResponse {

    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;

    public static AuthResponse from (AuthResponseDto authResponseDto) {
        return new AuthResponse(authResponseDto.getAccessToken(), authResponseDto.getRefreshToken());
    }

    public static AuthResponse of(String accessToken,String refreshToken){
        return new AuthResponse(accessToken,refreshToken);
    }

    private AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }


}
