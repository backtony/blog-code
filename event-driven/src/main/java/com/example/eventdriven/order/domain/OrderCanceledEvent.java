package com.example.eventdriven.order.domain;

import com.example.eventdriven.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCanceledEvent extends Event {
    private Long orderId;
}
