package com.example.orderservice.infrastructure;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public List<Order> findByUserId(String userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
}
