package com.springsecurity.authentication.presentation.dto;

import com.springsecurity.authentication.application.dto.SignUpRequestDto;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignUpRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    public SignUpRequestDto toSignUpRequestDto(){
        return SignUpRequestDto.of(name,email,password);
    }
}