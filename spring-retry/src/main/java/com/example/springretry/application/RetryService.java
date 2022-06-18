package com.example.springretry.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RetryService {

    private int count;

    @Retryable(
            value = RuntimeException.class, // retry 예외 대상
            maxAttempts = 3, // 3회 시도
            backoff = @Backoff(delay = 2000) // 재시도 시 2초 후 시도
    )
    public int retrySuccess(){
        count++;
        if (count % 2==0){
            return count;
        }
        throw new RuntimeException("runtime exception");
    }


    @Retryable(
            value = RuntimeException.class, // retry 예외 대상
            maxAttempts = 3, // 3회 시도
            backoff = @Backoff(delay = 2000) // 재시도 시 2초 후 시도
    )
    public int retryFail(String name){
        throw new RuntimeException("runtime exception");
    }

    // target 메서드와 반환값 일치
    // 메서드의 첫 인자는 예외, 이후 인자는 타겟 메서드와 타입을 일치 시켜야함
    @Recover
    public int recover(RuntimeException e, String name) {
        log.info("{}", name);
        return -1;
    }

    @Retryable(
            value = RuntimeException.class, // retry 예외 대상
            maxAttempts = 3, // 3회 시도
            backoff = @Backoff(delay = 2000), // 재시도 시 2초 후 시도
            recover = "recover2", // 특정 recover 지정법 -> 메서드 이름 명시
            listeners = "defaultRetryListener" // 빈 이름 등록
    )
    public int retryFail2(String name){
        log.info("service method call");
        throw new RuntimeException("runtime exception");

    }

    @Recover
    public int recover2(RuntimeException e, String name) {
        log.info("{}", name);
        return -2;
    }


    public int retryTemplate(){
        throw new RuntimeException("runtime exception");
    }

    public int retryTemplateRecover(){
        return -3;
    }
}
