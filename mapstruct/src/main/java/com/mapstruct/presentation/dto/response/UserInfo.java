package com.mapstruct.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfo {
    private String name;
    private int age;
}
