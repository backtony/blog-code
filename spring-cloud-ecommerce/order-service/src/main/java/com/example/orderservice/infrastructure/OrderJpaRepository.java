package com.example.orderservice.infrastructure;

import com.example.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order,Long> {


    List<Order> findByUserId(String userId);

}
