package com.mapstruct.domain.service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDto {

    private String name;
    private int age;
}
