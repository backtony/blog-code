package com.example.client.client.dto

import java.time.LocalDateTime

data class ArticleResponse(
    val id: String,
    var title: String,
    var body: String,
    val registeredDate: LocalDateTime
)

