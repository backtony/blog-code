package com.example.userservice.domain.service.dto.request;

import com.example.userservice.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserSaveRequestDto {

    private String email;
    private String name;
    private String pwd;
    private String userId;

    public User toEntity(){
        return User.builder()
                .email(email)
                .name(name)
                .pwd(pwd)
                .userId(UUID.randomUUID().toString())
                .build();
    }
}
