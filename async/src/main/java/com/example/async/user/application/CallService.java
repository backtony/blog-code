package com.example.async.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserService userService;

    public void voidCall(){
        userService.returnVoid();
        log.info("origin void call");
    }

    public void futureCall() {
        Future<String> future = userService.returnFuture();
        try {
            log.info("{}",future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin future call");
    }

    public void listenableFutureCall() {
        ListenableFuture<String> future = userService.returnListenableFuture();
        try {
            future.addCallback(s -> log.info(s),ex -> log.error(ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin listenableFuture call");
    }

    public void completableFutureCall() {
        CompletableFuture<String> future = userService.returnCompletableFuture();
        try {
            future.thenAccept(s -> log.info("{}",s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin completableFutureC call");
    }
}
