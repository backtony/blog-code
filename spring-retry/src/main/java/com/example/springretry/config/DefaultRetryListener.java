package com.example.springretry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

@Slf4j
public class DefaultRetryListener extends RetryListenerSupport {

    // 재시도 타겟 메서드(@Retryable이 붙은) 메서드 자체가 호출되기 전
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        // 로직 작성
        log.info("before call target method");
        return super.open(context, callback);
    }

    // 재시도가 전부 끝난 후에 호출
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // 로직 작성
        log.info("after retry");
        super.close(context, callback, throwable);
    }

    // 재시도 타겟 메서드에서 예외가 발생하면 호출
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // 로직 작성
        log.info("on error");
        super.onError(context, callback, throwable);
    }
}
