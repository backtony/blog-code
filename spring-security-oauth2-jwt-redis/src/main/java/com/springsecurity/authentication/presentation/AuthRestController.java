package com.springsecurity.authentication.presentation;

import com.springsecurity.authentication.application.AuthCommandUseCase;
import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.presentation.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthCommandUseCase authCommandUseCase;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String accessToken,
                                       @RequestHeader(value = "refreshToken") String refreshToken) {
        authCommandUseCase.logout(accessToken,refreshToken);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissueToken(@RequestHeader(value = "Authorization") String refreshToken){
        AuthResponseDto authResponseDto = authCommandUseCase.reissue(refreshToken);
        return ResponseEntity.ok(AuthResponse.from(authResponseDto));
    }

}
