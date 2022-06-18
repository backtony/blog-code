package com.example.orderservice.application;


import com.example.orderservice.domain.service.MessageQueueClient;
import com.example.orderservice.domain.service.OrderService;
import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final MessageQueueClient messageQueueClient;

    public OrderInfoResponseDto createOrder(OrderSaveRequestDto orderSaveRequestDto){
        OrderInfoResponseDto orderInfoResponseDto = orderService.createOrder(orderSaveRequestDto);
        messageQueueClient.send("example-catalog-topic",orderInfoResponseDto);
        return orderInfoResponseDto;
    }

    public List<OrderInfoResponseDto> getOrdersByUserId(String userId){
        return orderService.getOrdersByUserId(userId);
    }
}
