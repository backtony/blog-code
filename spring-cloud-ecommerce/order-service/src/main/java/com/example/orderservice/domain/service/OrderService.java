package com.example.orderservice.domain.service;

import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;

import java.util.List;

public interface OrderService {

    OrderInfoResponseDto createOrder(OrderSaveRequestDto orderSaveRequestDto);
    OrderInfoResponseDto getOrderByOrderId(String orderId);
    List<OrderInfoResponseDto> getOrdersByUserId(String userId);
}
