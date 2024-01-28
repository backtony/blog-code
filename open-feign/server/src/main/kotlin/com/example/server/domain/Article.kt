package com.example.server.domain

import java.time.LocalDateTime

data class Article(
    val id: String,
    var title: String,
    var body: String,
    val registeredDate: LocalDateTime
)


