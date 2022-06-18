package com.example.eventdriven.eventstore.domain;

public interface EventSender {
    void send(EventEntry event);
}
