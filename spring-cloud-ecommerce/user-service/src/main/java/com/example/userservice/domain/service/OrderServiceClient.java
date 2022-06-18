package com.example.userservice.domain.service;

import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderServiceClient {

    List<OrderResponseDto> getOrders(String userId);
}
