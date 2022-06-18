package com.example.orderservice.domain.service;

import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;

public interface MessageQueueClient {

    void send(String topic, OrderInfoResponseDto orderInfoResponseDto);
}
