package com.oauth2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FriendResourceServerApplication

fun main(args: Array<String>) {
    runApplication<FriendResourceServerApplication>(*args)
}
