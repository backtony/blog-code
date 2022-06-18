package com.example.eventdriven.eventstore.domain;

import java.util.List;

public interface EventStore {
    void save(Object event);

    List<EventEntry> get(long offset, long limit);
}
