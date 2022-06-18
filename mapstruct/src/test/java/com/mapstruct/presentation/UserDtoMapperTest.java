package com.mapstruct.presentation;

import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;
import com.mapstruct.domain.service.dto.response.UserInfoDto;
import com.mapstruct.domain.service.dto.response.UserResponseDto;
import com.mapstruct.presentation.dto.request.UserSaveRequest;
import com.mapstruct.presentation.dto.request.UserSaveRequest2;
import com.mapstruct.presentation.dto.request.UserSaveRequest3;
import com.mapstruct.presentation.dto.response.UserInfo;
import com.mapstruct.presentation.dto.response.UserInfoResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


public class UserDtoMapperTest {

    UserDtoMapper userDtoMapper = Mappers.getMapper(UserDtoMapper.class);

    @Test
    void userSaveRequestToUserSaveRequestDto() {
        //given
        UserSaveRequest userSaveRequest = new UserSaveRequest();
        userSaveRequest.setAddress("강남");
        userSaveRequest.setAge(27);
        userSaveRequest.setName("최준성");
        userSaveRequest.setNickname("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest);

        //then
        assertThat(result.getAddress()).isEqualTo(userSaveRequest.getAddress());
        assertThat(result.getAge()).isEqualTo(userSaveRequest.getAge());
        assertThat(result.getName()).isEqualTo(userSaveRequest.getName());
        assertThat(result.getNickname()).isEqualTo(userSaveRequest.getNickname());
    }

    @Test
    void userSaveRequest2ToUserSaveRequestDto() {
        //given
        UserSaveRequest2 userSaveRequest2 = new UserSaveRequest2();
        userSaveRequest2.setNickname2("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest2);

        //then
        assertThat(result.getNickname()).isEqualTo(userSaveRequest2.getNickname2());
    }

    @Test
    void userSaveRequest3ToUserSaveRequestDto() {
        //given
        UserSaveRequest3 userSaveRequest3 = new UserSaveRequest3();
        userSaveRequest3.setAddress("강남");
        userSaveRequest3.setAge(27);
        userSaveRequest3.setName("최준성");
        userSaveRequest3.setNickname("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest3);

        //then
        assertThat(result.getAddress()).isEqualTo(userSaveRequest3.getAddress());
        assertThat(result.getAge()).isEqualTo(userSaveRequest3.getAge());
        assertThat(result.getName()).isEqualTo(userSaveRequest3.getName());
        assertThat(result.getNickname()).isEqualTo(null);
    }

    @Test
    void userResponseDtoToUserInfoResponse() {
        //given
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .userInfoDtoList(List.of(UserInfoDto.builder()
                                .age(27)
                                .name("backtony1")
                                .build(),
                        UserInfoDto.builder()
                                .age(27)
                                .name("backtony2")
                                .build()))
                .address("강남")
                .build();

        //when
        UserInfoResponse result = userDtoMapper.from(userResponseDto);

        //then
        assertThat(result.getUserInfoDtoList().size()).isEqualTo(userResponseDto.getUserInfoDtoList().size());
        assertThat(result.getUserInfoDtoList().get(0).getAge()).isEqualTo(userResponseDto.getUserInfoDtoList().get(0).getAge());
        assertThat(result.getUserInfoDtoList().get(0).getName()).isEqualTo(userResponseDto.getUserInfoDtoList().get(0).getName());
        assertThat(result.getAddress()).isEqualTo(userResponseDto.getAddress());
    }

    @Test
    void userInfoDtoToUserInfoInfo() {
        //given
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .age(27)
                .name("backtony1")
                .build();

        //when
        UserInfo result = userDtoMapper.from(userInfoDto);

        //then
        assertThat(result.getName()).isEqualTo(userInfoDto.getName());
        assertThat(result.getAge()).isEqualTo(userInfoDto.getAge());
    }

}
