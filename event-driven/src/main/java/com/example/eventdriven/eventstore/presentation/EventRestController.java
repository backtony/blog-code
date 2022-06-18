package com.example.eventdriven.eventstore.presentation;

import com.example.eventdriven.eventstore.domain.EventEntry;
import com.example.eventdriven.eventstore.domain.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventRestController {

    private final EventStore eventStore;

    public List<EventEntry> list(
            @RequestParam("offset") Long offset,
            @RequestParam("limit") Long limit){
        return eventStore.get(offset,limit);
    }
}
