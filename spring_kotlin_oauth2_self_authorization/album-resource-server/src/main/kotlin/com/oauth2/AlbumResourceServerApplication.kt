package com.oauth2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AlbumResourceServerApplication

fun main(args: Array<String>) {
    runApplication<AlbumResourceServerApplication>(*args)
}
