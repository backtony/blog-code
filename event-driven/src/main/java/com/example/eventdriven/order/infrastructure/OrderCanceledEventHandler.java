package com.example.eventdriven.order.infrastructure;

import com.example.eventdriven.order.domain.OrderCanceledEvent;
import com.example.eventdriven.refund.application.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCanceledEventHandler {

    private final RefundService refundService;

    @Async
    @TransactionalEventListener(
            classes = OrderCanceledEvent.class,
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(OrderCanceledEvent event){
        refundService.refund(event.getOrderId());
    }

}
