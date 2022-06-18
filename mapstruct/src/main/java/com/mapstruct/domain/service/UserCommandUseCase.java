package com.mapstruct.domain.service;

import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;

public interface UserCommandUseCase {

    void registerUser(UserSaveRequestDto userSaveRequestDto);
}
