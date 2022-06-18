package com.example.eventdriven.common.event;

import com.example.eventdriven.eventstore.domain.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventStoreHandler {
    private final EventStore eventStore;

    @EventListener(Event.class)
    public void handle(Event event) {
        eventStore.save(event);
    }
}
