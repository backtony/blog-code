package com.example.orderservice.presentation.dto;

import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import com.example.orderservice.presentation.dto.request.OrderSaveRequest;
import com.example.orderservice.presentation.dto.response.OrderInfoResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-24T09:05:27+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (AdoptOpenJDK)"
)
@Component
public class OrderDtoMapperImpl implements OrderDtoMapper {

    @Override
    public OrderSaveRequestDto from(OrderSaveRequest orderSaveRequest) {
        if ( orderSaveRequest == null ) {
            return null;
        }

        OrderSaveRequestDto orderSaveRequestDto = new OrderSaveRequestDto();

        orderSaveRequestDto.setProductId( orderSaveRequest.getProductId() );
        orderSaveRequestDto.setQty( orderSaveRequest.getQty() );
        orderSaveRequestDto.setUnitPrice( orderSaveRequest.getUnitPrice() );

        return orderSaveRequestDto;
    }

    @Override
    public OrderInfoResponse from(OrderInfoResponseDto orderInfoResponseDto) {
        if ( orderInfoResponseDto == null ) {
            return null;
        }

        OrderInfoResponse orderInfoResponse = new OrderInfoResponse();

        orderInfoResponse.setProductId( orderInfoResponseDto.getProductId() );
        orderInfoResponse.setQty( orderInfoResponseDto.getQty() );
        orderInfoResponse.setUnitPrice( orderInfoResponseDto.getUnitPrice() );
        orderInfoResponse.setTotalPrice( orderInfoResponseDto.getTotalPrice() );
        orderInfoResponse.setOrderId( orderInfoResponseDto.getOrderId() );

        return orderInfoResponse;
    }
}
