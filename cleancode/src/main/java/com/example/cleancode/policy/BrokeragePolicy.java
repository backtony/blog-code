package com.example.cleancode.policy;

import com.example.cleancode.exception.ErrorCode;
import com.example.cleancode.exception.HouseUtilsException;

import java.util.List;

public interface BrokeragePolicy {

    List<BrokerageRule> getRules();

    default Long calculate(Long price){
        BrokerageRule brokerageRule = getRules().stream()
                .filter(rule -> price < rule.getLessThan())
                .findFirst().orElseThrow(() -> new HouseUtilsException(ErrorCode.INTERNAL_ERROR));
        return brokerageRule.cacMaxBrokerage(price);
    }

}
