package com.springsecurity.authentication.application;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.application.dto.SignUpRequestDto;
import com.springsecurity.authentication.domain.LogoutAccessToken;
import com.springsecurity.authentication.domain.LogoutRefreshToken;
import com.springsecurity.authentication.domain.TokenRepository;
import com.springsecurity.common.exception.user.AlreadyExistsUserException;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements AuthCommandUseCase {

    private final TokenProvider tokenProvider;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void localSignUp(SignUpRequestDto signUpRequestDto) {
        checkIsAlreadyExistsUser(signUpRequestDto);
        encodingPassword(signUpRequestDto);
        userRepository.save(signUpRequestDto.toLocalUserEntity());
    }


    private void checkIsAlreadyExistsUser(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByEmailAndAuthProvider(signUpRequestDto.getEmail(), AuthProvider.local)){
            throw new AlreadyExistsUserException();
        }
    }

    private void encodingPassword(SignUpRequestDto signUpRequestDto) {
        String encodePassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        signUpRequestDto.setPassword(encodePassword);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        saveLogoutAccessToken(accessToken);
        saveLogoutRefreshToken(refreshToken);
    }

    private void saveLogoutAccessToken(String accessToken) {
        String removedTypeAccessToken = getRemovedBearerType(accessToken);
        LogoutAccessToken logoutAccessToken = LogoutAccessToken.of(removedTypeAccessToken,
                getRemainingMilliSecondsFromToken(removedTypeAccessToken));
        tokenRepository.saveLogoutAccessToken(logoutAccessToken);
    }

    private void saveLogoutRefreshToken(String refreshToken) {
        String removedTypeRefreshToken = getRemovedBearerType(refreshToken);
        LogoutRefreshToken logoutRefreshToken = LogoutRefreshToken.of(removedTypeRefreshToken,
                getRemainingMilliSecondsFromToken(removedTypeRefreshToken));
        tokenRepository.saveLogoutRefreshToken(logoutRefreshToken);
    }

    private String getRemovedBearerType(String token){
        return token.substring(7);
    }

    private long getRemainingMilliSecondsFromToken(String removedTypeToken) {
        return tokenProvider.getRemainingMilliSecondsFromToken(removedTypeToken);
    }

    @Override
    public AuthResponseDto reissue(String refreshToken) {
        saveLogoutRefreshToken(refreshToken);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String newAccessToken = tokenProvider.createAccessToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(authentication);
        return AuthResponseDto.of(newAccessToken, newRefreshToken);
    }
}
