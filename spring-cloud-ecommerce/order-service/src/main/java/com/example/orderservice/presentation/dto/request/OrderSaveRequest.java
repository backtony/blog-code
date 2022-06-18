package com.example.orderservice.presentation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSaveRequest {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
}
