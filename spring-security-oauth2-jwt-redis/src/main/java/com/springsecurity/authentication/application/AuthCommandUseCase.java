package com.springsecurity.authentication.application;

import com.springsecurity.authentication.application.dto.AuthResponseDto;

public interface AuthCommandUseCase {

    void logout(String accessToken, String refreshToken);

    AuthResponseDto reissue(String refreshToken);

}
