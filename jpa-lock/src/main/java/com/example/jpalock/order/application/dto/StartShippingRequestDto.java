package com.example.jpalock.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartShippingRequestDto {

    private Long orderId;
    private long version;

}
