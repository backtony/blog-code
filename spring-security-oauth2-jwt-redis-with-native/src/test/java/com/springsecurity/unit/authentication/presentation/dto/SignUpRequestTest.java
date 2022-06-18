package com.springsecurity.unit.authentication.presentation.dto;

import com.springsecurity.authentication.application.dto.SignUpRequestDto;
import com.springsecurity.authentication.presentation.dto.SignUpRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpRequestTest {

    @Test
    void signUpRequest를_signUpRequestDto로_변환() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .name("backtony")
                .email("backtony@gmail.com")
                .password("password")
                .build();

        //when
        SignUpRequestDto signUpRequestDto = signUpRequest.toSignUpRequestDto();

        //then
        assertThat(signUpRequestDto.getEmail()).isEqualTo(signUpRequest.getEmail());
        assertThat(signUpRequestDto.getName()).isEqualTo(signUpRequest.getName());
        assertThat(signUpRequestDto.getPassword()).isEqualTo(signUpRequest.getPassword());
    }
}
