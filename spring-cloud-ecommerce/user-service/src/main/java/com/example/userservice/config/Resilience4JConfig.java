package com.example.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4JConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomResilience4JConfig(){

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                // circuitBreaker를 열지 닫을지 결정하는 퍼센트 -> 4는 4퍼센트로 100번중 4번이 실패하면 서킷브러에키러르 연다.
                // default는 50
                .failureRateThreshold(4)
                // circuitBreaker를 open한 이후 유지하는 시간
                // default는 60초
                .waitDurationInOpenState(Duration.ofMillis(1000))
                // 어느 순간 정상적으로 동작하게 되면 circuitBreaker가 닫히게 되는데 이때 지금까지 호출했던 결과값을 저장하기 위해 카운트 기반으로 할지 시간 기반할지 선택
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // circuitBreaker가 닫힐 때 호출 결과를 기록하는 데 사용되는 슬라이딩 창의 크기
                // default는 100
                .slidingWindowSize(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                // 어느정도 까지 문제가 생겼을 때 4초동안 통신 응답이 없으면 circuitBreaker를 연다.
                .timeoutDuration(Duration.ofSeconds(4))
                .build();

        return factory -> factory.configureDefault( id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build());
    }
}
