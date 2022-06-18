package com.mapstruct.domain.service.dto.request;

import com.mapstruct.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class UserSaveRequestDto {
    private String nickname;

    private String address;

    private String name;

    private int age;

    public User toEntity(){
        return User.builder()
                .nickname(nickname)
                .address(address)
                .age(age)
                .name(name)
                .build();
    }
}
