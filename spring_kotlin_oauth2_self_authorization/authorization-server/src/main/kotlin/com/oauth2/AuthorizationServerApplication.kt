package com.oauth2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class AuthorizationServerApplication

fun main(args: Array<String>) {
    runApplication<AuthorizationServerApplication>(*args)
}
