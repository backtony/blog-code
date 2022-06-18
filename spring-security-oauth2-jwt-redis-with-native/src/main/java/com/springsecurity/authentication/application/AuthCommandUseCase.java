package com.springsecurity.authentication.application;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.application.dto.SignUpRequestDto;

public interface AuthCommandUseCase {

    void localSignUp(SignUpRequestDto signUpRequestDto);

    void logout(String accessToken, String refreshToken);

    AuthResponseDto reissue(String refreshToken);


}
