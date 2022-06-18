package com.example.jpalock.order.application;

import com.example.jpalock.common.exception.order.VersionConflictException;
import com.example.jpalock.order.application.dto.StartShippingRequestDto;
import com.example.jpalock.order.domain.Order;
import com.example.jpalock.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private OrderRepository orderRepository;

    @Transactional
    public void startShipping(StartShippingRequestDto req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("order not found"));
        if (order.matchVersion(req.getVersion())) {
            throw new VersionConflictException();
        }
        order.startShipping();
    }
}
