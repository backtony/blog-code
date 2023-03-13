package com.springkotlinbatch.common.exeception.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val status: String,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
