package com.example.userservice.infrastructure;

import com.example.userservice.domain.service.OrderServiceClient;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClientImpl implements OrderServiceClient {

    private final OrderServiceFeignClient orderServiceFeignClient;

    @Override
    public List<OrderResponseDto> getOrders(String userId) {
        return orderServiceFeignClient.getOrders(userId);
    }
}
