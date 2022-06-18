package com.example.jpalock.order.presentation;

import com.example.jpalock.common.exception.order.VersionConflictException;
import com.example.jpalock.order.application.OrderService;
import com.example.jpalock.order.presentation.dto.StartShippingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping
    public void start(@RequestBody StartShippingRequest startShippingRequest){
        try {
            orderService.startShipping(startShippingRequest.toStartShippingRequestDto());
        } catch (OptimisticLockingFailureException | VersionConflictException e){
            log.error("{}","낙관적락 예외 발생");
        }
    }
}
