package com.example.userservice.presentation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoResponse {
    private String email;
    private String name;
    private String userId;

    private List<OrderResponse> orders;
}
