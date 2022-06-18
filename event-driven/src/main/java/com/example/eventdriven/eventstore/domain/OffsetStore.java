package com.example.eventdriven.eventstore.domain;

public interface OffsetStore {
    long get();
    void update(long nextOffset);
}
