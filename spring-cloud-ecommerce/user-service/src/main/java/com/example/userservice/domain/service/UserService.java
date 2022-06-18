package com.example.userservice.domain.service;

import com.example.userservice.domain.service.dto.request.UserSaveRequestDto;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;

public interface UserService {

    UserInfoResponseDto createUser(UserSaveRequestDto userSaveRequestDto);

    UserInfoResponseDto getUserInfo(String userId);
}
