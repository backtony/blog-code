package com.example.eventdriven.eventstore.domain;

public class PayloadConvertException extends RuntimeException {
    public PayloadConvertException(Exception e) {
        super(e);
    }
}
