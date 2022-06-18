package com.example.userservice.domain.service.dto.response;

import com.example.userservice.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponseDto {

    private String email;
    private String name;
    private String userId;
    private List<OrderResponseDto> orders;

    public static UserInfoResponseDto from(User user){
        return UserInfoResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
