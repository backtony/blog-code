package com.mapstruct.domain.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class UserResponseDto {

    private String address;

    private List<UserInfoDto> userInfoDtoList;
}
