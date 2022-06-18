package com.mapstruct.presentation.dto.request;

import lombok.*;


@Getter
@Setter
public class UserSaveRequest {

    private String nickname;

    private String address;

    private String name;

    private int age;
}
