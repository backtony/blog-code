package com.example.eventdriven.order.application;

import com.example.eventdriven.order.domain.Order;
import com.example.eventdriven.order.domain.OrderCanceledEvent;
import com.example.eventdriven.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;
    private final OrderRepository orderRepository;

    public void cancel(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("order not found"));

        order.cancel();

        eventPublisher.publishEvent(new OrderCanceledEvent(orderId));
    }

    public void save() {
        orderRepository.save(Order.builder()
                .status(Order.Status.ORDER)
                .build());
    }
}
