package com.example.catalogservice.domain.service.dto.response;

import com.example.catalogservice.domain.Catalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CatalogInfoResponseDto {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;

    public static CatalogInfoResponseDto from(Catalog catalog){
        return CatalogInfoResponseDto.builder()
                .productId(catalog.getProductId())
                .qty(catalog.getStock())
                .unitPrice(catalog.getUnitPrice())
                .build();
    }
}
