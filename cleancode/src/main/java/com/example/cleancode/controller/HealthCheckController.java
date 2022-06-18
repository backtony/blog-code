package com.example.cleancode.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author backtony
 *
 * 서비스 헬스 체크를 위한 컨트롤러
 */

@RestController
public class HealthCheckController {

    @GetMapping("/api/ping")
    public String ping(){
        return "ok";
    }
}
