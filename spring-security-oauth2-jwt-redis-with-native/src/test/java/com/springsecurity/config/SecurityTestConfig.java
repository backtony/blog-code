package com.springsecurity.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.springsecurity.config.security")
public class SecurityTestConfig {
}
