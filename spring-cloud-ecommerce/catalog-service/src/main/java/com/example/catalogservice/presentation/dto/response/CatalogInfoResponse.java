package com.example.catalogservice.presentation.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogInfoResponse {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;
}
