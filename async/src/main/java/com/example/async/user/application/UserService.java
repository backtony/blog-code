package com.example.async.user.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
public class UserService {

    @Async("EVENT_HANDLER_TASK_EXECUTOR")
    public void event(){
        log.info("{}","event");
    }

    @Async("HELLO_WORLD_EXECUTOR")
    public void hello(){
        log.info("{}","hello");
    }

    @Async
    public void returnVoid(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return void");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Async
    public Future<String> returnFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return future");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return future");
    }

    @Async
    public ListenableFuture<String> returnListenableFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return ListenableFuture");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return ListenableFuture");
    }

    @Async
    public CompletableFuture<String> returnCompletableFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return CompletableFuture");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return CompletableFuture").completable();
    }
}
