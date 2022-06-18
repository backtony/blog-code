package com.example.userservice.presentation.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderResponse {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private String orderId;
}
