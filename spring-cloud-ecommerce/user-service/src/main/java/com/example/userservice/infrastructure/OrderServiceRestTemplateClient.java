package com.example.userservice.infrastructure;

import com.example.userservice.domain.service.OrderServiceClient;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderServiceRestTemplateClient {

    private final RestTemplate restTemplate;
    private final Environment env;

    public List<OrderResponseDto> getOrders(String userId) {
        String orderUrl = String.format(env.getProperty("order_service.url"),userId);
        ResponseEntity<List<OrderResponseDto>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<OrderResponseDto>>() {
                });
        return orderListResponse.getBody();
    }
}
