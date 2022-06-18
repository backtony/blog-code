package com.example.orderservice.domain.service.dto.request;

import com.example.orderservice.domain.Order;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderSaveRequestDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String userId;

    public Order toEntity(){
        return Order.builder()
                .productId(productId)
                .qty(qty)
                .unitPrice(unitPrice)
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .totalPrice(totalPrice)
                .build();
    }
}
