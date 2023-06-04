package com.example.client.controller.advice

import java.time.LocalDateTime

data class ErrorResponse(
    val status: String,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
