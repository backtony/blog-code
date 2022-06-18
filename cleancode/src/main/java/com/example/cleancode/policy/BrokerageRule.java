package com.example.cleancode.policy;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * 가격이 특정 범위일 때 상한효율과 상한 금액이 가지는 클래스
 */
@AllArgsConstructor
@Getter
public class BrokerageRule {
    private Long lessThan;
    private Double brokeragePercent;

    @Nullable // null이 들어올 수 있다는 주석 annotation
    private Long limitAmount;

    public BrokerageRule(Long lessThan, Double brokeragePercent) {
        this(lessThan,brokeragePercent,Long.MAX_VALUE);
    }

    public Long cacMaxBrokerage(Long price){
        return Math.min(multiplyPercent(price),limitAmount);
    }

    private long multiplyPercent(Long price) {
        return Double.valueOf(Math.floor(brokeragePercent / 100 * price)).longValue();
    }
}
