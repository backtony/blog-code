package com.example.orderservice.domain.service;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderRepository;
import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;


    @Override
    public OrderInfoResponseDto createOrder(OrderSaveRequestDto orderSaveRequestDto) {
        orderSaveRequestDto.setTotalPrice(orderSaveRequestDto.getUnitPrice() * orderSaveRequestDto.getQty());
        Order order = orderRepository.save(orderSaveRequestDto.toEntity());
        return OrderInfoResponseDto.from(order);
    }

    @Override
    public OrderInfoResponseDto getOrderByOrderId(String orderId) {
        return null;
    }

    @Override
    public List<OrderInfoResponseDto> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderInfoResponseDto::from)
                .collect(Collectors.toList());
    }
}
