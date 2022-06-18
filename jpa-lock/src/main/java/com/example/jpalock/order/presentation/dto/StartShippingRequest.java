package com.example.jpalock.order.presentation.dto;

import com.example.jpalock.order.application.dto.StartShippingRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartShippingRequest {
    private Long orderId;
    private long version;

    public StartShippingRequestDto toStartShippingRequestDto(){
        return new StartShippingRequestDto(orderId,version);
    }
}
