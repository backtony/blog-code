package com.mapstruct.application;

import com.mapstruct.domain.service.UserCommandUseCase;
import com.mapstruct.domain.service.UserQueryUseCase;
import com.mapstruct.domain.service.dto.response.UserResponseDto;
import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserCommandUseCase userCommandUseCase;
    private final UserQueryUseCase userQueryUseCase;

    public void registerUser(UserSaveRequestDto userSaveRequestDto){
        userCommandUseCase.registerUser(userSaveRequestDto);
    }

    public UserResponseDto getSameAddressUserInfo() {
        return userQueryUseCase.getSameAddressUserInfo();
    }

}
