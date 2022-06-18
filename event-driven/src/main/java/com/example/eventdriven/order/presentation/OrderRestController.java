package com.example.eventdriven.order.presentation;

import com.example.eventdriven.order.application.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping
    public void save(){
        orderService.save();
    }

    @PostMapping("{orderId}")
    public void cancel(@PathVariable Long orderId){
        orderService.cancel(orderId);
    }
}
