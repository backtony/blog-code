package com.example.demo.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class CorsController {

    private AtomicLong atomicLong = new AtomicLong(0L);

    @PutMapping("/hello")
    public String hello() {
        return "put hello" + atomicLong.incrementAndGet();
    }

    @GetMapping("/hello")
    public String getHello() {
        return "get hello"+ atomicLong.incrementAndGet();
    }

    @PostMapping("/hello")
    public String postHello() {
        return "post hello"+ atomicLong.incrementAndGet();
    }

    @DeleteMapping("/hello")
    public String deleteHello() {
        return "delete hello"+ atomicLong.incrementAndGet();
    }
}
