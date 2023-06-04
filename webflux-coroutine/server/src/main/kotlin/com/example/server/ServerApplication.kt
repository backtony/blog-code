package com.example.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    Hooks.onOperatorDebug()
    runApplication<ServerApplication>(*args)
}
