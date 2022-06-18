package com.mapstruct.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserInfoResponse {

    private String address;

    List<UserInfo> userInfoDtoList;
}
