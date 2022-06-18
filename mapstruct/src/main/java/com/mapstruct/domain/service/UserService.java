package com.mapstruct.domain.service;

import com.mapstruct.domain.repository.UserStore;
import com.mapstruct.domain.service.dto.response.UserInfoDto;
import com.mapstruct.domain.service.dto.response.UserResponseDto;
import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserCommandUseCase, UserQueryUseCase{

    private final UserStore userStore;

    @Override
    public void registerUser(UserSaveRequestDto userSaveRequestDto) {
        userStore.store(userSaveRequestDto.toEntity());
    }

    @Override
    public UserResponseDto getSameAddressUserInfo() {
        return UserResponseDto.builder()
                .address("강남")
                .userInfoDtoList(List.of(
                        createUserInfoDto(21,"hello1"),
                        createUserInfoDto(21,"hello2"),
                        createUserInfoDto(21,"hello3"),
                        createUserInfoDto(21,"hello4")
                ))
                .build();
    }

    private UserInfoDto createUserInfoDto(int age, String name) {
        return UserInfoDto.builder()
                .age(age)
                .name(name)
                .build();
    }
}
