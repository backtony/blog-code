package com.springsecurity.authentication.application.dto;

import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.Role;
import com.springsecurity.user.domain.User;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignUpRequestDto {
    private String name;

    private String email;

    private String password;

    public static SignUpRequestDto of(String name, String email, String password){
        return SignUpRequestDto.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }

    public User toLocalUserEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .authProvider(AuthProvider.local)
                .role(Role.USER)
                .build();
    }
}
