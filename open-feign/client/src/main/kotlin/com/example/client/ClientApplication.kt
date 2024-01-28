package com.example.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

/**
 * @EnableFeignClients : 지정된 패키지를 돌아다니면서 @FeignCleint를 찾아 구현체를 만들어 준다.
 */
@EnableFeignClients
@SpringBootApplication
class ClientApplication

fun main(args: Array<String>) {
    runApplication<ClientApplication>(*args)
}
