package com.example.userservice.presentation.dto;

import com.example.userservice.domain.service.dto.request.UserSaveRequestDto;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;
import com.example.userservice.presentation.dto.request.UserSaveRequest;
import com.example.userservice.presentation.dto.response.OrderResponse;
import com.example.userservice.presentation.dto.response.UserInfoResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserDtoMapper {

    @Mapping(target = "userId",ignore = true)
    UserSaveRequestDto from(UserSaveRequest userSaveRequest);

    UserInfoResponse from (UserInfoResponseDto userInfoResponseDto);

    OrderResponse from (OrderResponseDto orderResponseDto);
}

