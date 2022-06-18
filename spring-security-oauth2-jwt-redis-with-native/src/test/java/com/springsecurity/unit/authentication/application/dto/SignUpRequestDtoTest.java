package com.springsecurity.unit.authentication.application.dto;

import com.springsecurity.authentication.application.dto.SignUpRequestDto;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.Role;
import com.springsecurity.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpRequestDtoTest {
    @Test
    void of_정적_메서드로_생성() throws Exception{
        //given
        String name = "backtony";
        String email = "backtony@gmail.com";
        String password = "password";

        //when
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.of(name, email, password);

        //then
        assertThat(signUpRequestDto.getName()).isEqualTo(name);
        assertThat(signUpRequestDto.getEmail()).isEqualTo(email);
        assertThat(signUpRequestDto.getPassword()).isEqualTo(password);
    }

    @Test
    void SignUpRequestDto_userEntity로_변환() throws Exception{
        //given
        String name = "backtony";
        String email = "backtony@gmail.com";
        String password = "password";
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.of(name, email, password);

        //when
        User user = signUpRequestDto.toLocalUserEntity();

        //then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.local);
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

}
