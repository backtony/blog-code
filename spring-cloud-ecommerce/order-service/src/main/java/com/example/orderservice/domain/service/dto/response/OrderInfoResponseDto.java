package com.example.orderservice.domain.service.dto.response;

import com.example.orderservice.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderInfoResponseDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;

    public static OrderInfoResponseDto from(Order order){
        return OrderInfoResponseDto.builder()
                .productId(order.getProductId())
                .qty(order.getQty())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .orderId(order.getOrderId())
                .build();
    }
}
