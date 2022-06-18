package com.example.orderservice.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findByUserId(String userId);

    List<Order> findAll();

    Order save(Order order);
}
