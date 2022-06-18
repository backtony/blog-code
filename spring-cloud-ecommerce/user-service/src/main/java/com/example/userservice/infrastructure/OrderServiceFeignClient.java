package com.example.userservice.infrastructure;

import com.example.userservice.domain.service.OrderServiceClient;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service") // 유레카에 등록된 application-name
public interface OrderServiceFeignClient{

    @GetMapping("/api/v1/{userId}/orders")
    List<OrderResponseDto> getOrders(@PathVariable("userId") String userId);

}
