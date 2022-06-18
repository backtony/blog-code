package com.example.orderservice.presentation.dto;

import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import com.example.orderservice.presentation.dto.request.OrderSaveRequest;
import com.example.orderservice.presentation.dto.response.OrderInfoResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderDtoMapper {

    @Mappings({
            @Mapping(target = "userId",ignore = true),
            @Mapping(target = "totalPrice",ignore = true)
    })
    OrderSaveRequestDto from (OrderSaveRequest orderSaveRequest);

    OrderInfoResponse from(OrderInfoResponseDto orderInfoResponseDto);
}
