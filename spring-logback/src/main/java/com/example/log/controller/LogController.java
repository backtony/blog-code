package com.example.log.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

//@Slf4j
@RestController
public class LogController {

    // @Slf4j 애노테이션으로 생략이 가능한 코드
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/log")
    public String logTest(){
        String name = "spring";

        //  {}는 쉼표 뒤에 파라미터가 치환되는 것
        log.error("error log={}", LocalDateTime.now());
        log.warn("warn log={}",LocalDateTime.now());
        log.info("info log={}",LocalDateTime.now());
        log.debug("debug log={}",LocalDateTime.now());
        log.trace("trace log={}",LocalDateTime.now());

        return "ok";
    }
}
