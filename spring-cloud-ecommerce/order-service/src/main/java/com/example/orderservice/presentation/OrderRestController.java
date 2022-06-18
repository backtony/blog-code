package com.example.orderservice.presentation;

import com.example.orderservice.application.OrderFacade;
import com.example.orderservice.domain.service.dto.request.OrderSaveRequestDto;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import com.example.orderservice.presentation.dto.OrderDtoMapper;
import com.example.orderservice.presentation.dto.request.OrderSaveRequest;
import com.example.orderservice.presentation.dto.response.OrderInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderFacade orderFacade;
    private final OrderDtoMapper orderDtoMapper;

    @PostMapping("/{userId}/orders")
    public ResponseEntity<OrderInfoResponse> createOrder(@PathVariable("userId") String userId,
                                                         @RequestBody OrderSaveRequest orderSaveRequest) {
        log.info("before add orders data");
        OrderSaveRequestDto orderSaveRequestDto = orderDtoMapper.from(orderSaveRequest);
        orderSaveRequestDto.setUserId(userId);

        OrderInfoResponseDto orderInfoResponseDto = orderFacade.createOrder(orderSaveRequestDto);
        OrderInfoResponse orderInfoResponse = orderDtoMapper.from(orderInfoResponseDto);
        log.info("after add orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderInfoResponse);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<OrderInfoResponse>> getOrder(@PathVariable("userId") String userId) {
        log.info("before retrieved orders data");
        List<OrderInfoResponse> orderInfoResponseList = orderFacade.getOrdersByUserId(userId).stream()
                .map(orderDtoMapper::from)
                .collect(Collectors.toList());
        log.info("after retrieved orders data");
        return ResponseEntity.ok(orderInfoResponseList);
    }
}
