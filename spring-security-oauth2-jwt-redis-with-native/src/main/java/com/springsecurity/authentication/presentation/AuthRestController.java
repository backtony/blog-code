package com.springsecurity.authentication.presentation;

import com.springsecurity.authentication.application.AuthCommandUseCase;
import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.presentation.dto.AuthResponse;
import com.springsecurity.authentication.presentation.dto.SignUpRequest;
import com.springsecurity.common.exception.ValidatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthCommandUseCase authCommandUseCase;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/sign-up")
    public ResponseEntity<Void> localSignUp(@Validated @RequestBody SignUpRequest signUpRequest, BindingResult errors){

        if (errors.hasErrors()) {
            throw new ValidatedException(errors);
        }

        authCommandUseCase.localSignUp(signUpRequest.toSignUpRequestDto());
        return ResponseEntity.ok().build();
    }



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
