package com.example.server.controller.dto

import java.time.LocalDateTime

data class ArticleSaveCommand(
    val title: String,
    val body: String,
    val requestDate: LocalDateTime,
)
