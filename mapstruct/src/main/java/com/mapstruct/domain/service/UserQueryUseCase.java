package com.mapstruct.domain.service;

import com.mapstruct.domain.service.dto.response.UserResponseDto;

public interface UserQueryUseCase {

    UserResponseDto getSameAddressUserInfo();
}
