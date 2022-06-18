package com.example.userservice.presentation.dto;

import com.example.userservice.domain.service.dto.request.UserSaveRequestDto;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;
import com.example.userservice.presentation.dto.request.UserSaveRequest;
import com.example.userservice.presentation.dto.response.OrderResponse;
import com.example.userservice.presentation.dto.response.UserInfoResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-24T09:05:32+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (AdoptOpenJDK)"
)
@Component
public class UserDtoMapperImpl implements UserDtoMapper {

    @Override
    public UserSaveRequestDto from(UserSaveRequest userSaveRequest) {
        if ( userSaveRequest == null ) {
            return null;
        }

        String email = null;
        String name = null;
        String pwd = null;

        email = userSaveRequest.getEmail();
        name = userSaveRequest.getName();
        pwd = userSaveRequest.getPwd();

        String userId = null;

        UserSaveRequestDto userSaveRequestDto = new UserSaveRequestDto( email, name, pwd, userId );

        return userSaveRequestDto;
    }

    @Override
    public UserInfoResponse from(UserInfoResponseDto userInfoResponseDto) {
        if ( userInfoResponseDto == null ) {
            return null;
        }

        UserInfoResponse userInfoResponse = new UserInfoResponse();

        userInfoResponse.setEmail( userInfoResponseDto.getEmail() );
        userInfoResponse.setName( userInfoResponseDto.getName() );
        userInfoResponse.setUserId( userInfoResponseDto.getUserId() );
        userInfoResponse.setOrders( orderResponseDtoListToOrderResponseList( userInfoResponseDto.getOrders() ) );

        return userInfoResponse;
    }

    @Override
    public OrderResponse from(OrderResponseDto orderResponseDto) {
        if ( orderResponseDto == null ) {
            return null;
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setProductId( orderResponseDto.getProductId() );
        orderResponse.setQty( orderResponseDto.getQty() );
        orderResponse.setUnitPrice( orderResponseDto.getUnitPrice() );
        orderResponse.setTotalPrice( orderResponseDto.getTotalPrice() );
        orderResponse.setOrderId( orderResponseDto.getOrderId() );

        return orderResponse;
    }

    protected List<OrderResponse> orderResponseDtoListToOrderResponseList(List<OrderResponseDto> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderResponse> list1 = new ArrayList<OrderResponse>( list.size() );
        for ( OrderResponseDto orderResponseDto : list ) {
            list1.add( from( orderResponseDto ) );
        }

        return list1;
    }
}
