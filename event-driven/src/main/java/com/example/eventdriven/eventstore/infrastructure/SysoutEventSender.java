package com.example.eventdriven.eventstore.infrastructure;

import com.example.eventdriven.eventstore.domain.EventEntry;
import com.example.eventdriven.eventstore.domain.EventSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SysoutEventSender implements EventSender {

    @Override
    public void send(EventEntry event) {
        // 외부 메시징 시스템에 이벤트를 전송하거나
        // 원하는 핸들러에 이벤트를 전달하면 된다.
        log.info("eventSender send event");
    }
}
