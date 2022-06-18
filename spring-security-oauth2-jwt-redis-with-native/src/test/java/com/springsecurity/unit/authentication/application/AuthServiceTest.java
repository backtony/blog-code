package com.springsecurity.unit.authentication.application;

import com.springsecurity.authentication.application.AuthService;
import com.springsecurity.authentication.application.TokenProvider;
import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.application.dto.SignUpRequestDto;
import com.springsecurity.authentication.domain.TokenRepository;
import com.springsecurity.common.exception.user.AlreadyExistsUserException;
import com.springsecurity.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks AuthService authService;
    @Mock TokenProvider tokenProvider;
    @Mock TokenRepository tokenRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void 로그아웃() throws Exception{
        //given
        String accessToken = "Bearer accessToken";
        String refreshToken = "Bearer accessToken";

        //when
        authService.logout(accessToken,refreshToken);

        //then
        then(tokenRepository).should().saveLogoutAccessToken(any());
        then(tokenRepository).should().saveLogoutRefreshToken(any());
    }

    @Test
    void 토큰_재발급() throws Exception{
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createAccessToken(any())).willReturn(accessToken);
        given(tokenProvider.createRefreshToken(any())).willReturn(refreshToken);

        //when
        AuthResponseDto authResponseDto = authService.reissue(refreshToken);

        //then
        assertThat(authResponseDto.getTokenType()).isEqualTo(authResponseDto.getTokenType());
        assertThat(authResponseDto.getAccessToken()).isEqualTo(accessToken);
        assertThat(authResponseDto.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void 회원가입_성공() throws Exception{
        //given
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.of("backtony", "backtony@gmail.com", "password");

        given(userRepository.existsByEmailAndAuthProvider(any(),any())).willReturn(false);

        //when
        authService.localSignUp(signUpRequestDto);

        //then
        then(passwordEncoder).should().encode(any());
        then(userRepository).should().save(any());
    }

    @Test
    void 회원가입_중복_실패() throws Exception{
        //given
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.of("backtony", "backtony@gmail.com", "password");

        given(userRepository.existsByEmailAndAuthProvider(any(),any())).willReturn(true);

        // when then
        assertThatThrownBy(() -> authService.localSignUp(signUpRequestDto)).isInstanceOf(AlreadyExistsUserException.class);
        then(passwordEncoder).should(never()).encode(any());
        then(userRepository).should(never()).save(any());
    }

}
