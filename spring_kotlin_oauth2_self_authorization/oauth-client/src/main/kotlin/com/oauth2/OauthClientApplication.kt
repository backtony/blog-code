package com.oauth2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OauthClientApplication

fun main(args: Array<String>) {
    runApplication<OauthClientApplication>(*args)
}
