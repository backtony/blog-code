package com.example.userservice.application;

import com.example.userservice.domain.service.UserService;
import com.example.userservice.domain.service.dto.request.UserSaveRequestDto;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    public UserInfoResponseDto createUser(UserSaveRequestDto userSaveRequestDto) {
        return userService.createUser(userSaveRequestDto);
    }

    public UserInfoResponseDto getUserInfo(String userId) {
        return userService.getUserInfo(userId);
    }
}
