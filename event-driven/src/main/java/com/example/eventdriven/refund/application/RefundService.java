package com.example.eventdriven.refund.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundService {

    public void refund(Long orderId){
        log.info("{} 가 환불 처리 되었습니다.",orderId);
    }
}