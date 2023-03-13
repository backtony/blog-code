package com.example.springmasterslave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringMasterSlaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMasterSlaveApplication.class, args);
    }

}
