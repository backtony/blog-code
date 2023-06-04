package com.example.client.controller.advice

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange

@RestControllerAdvice
class GlobalExceptionHandler {

    // https://www.baeldung.com/kotlin/kotlin-logging-library
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [RuntimeException::class])
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    suspend fun handleRuntimeException(e: RuntimeException, exchange: ServerWebExchange): ErrorResponse {
        logger.error("occurred error!: ${e.message}", e)
        return ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            error = e.javaClass.simpleName,
            path = exchange.request.path.toString(),
            message = e.message!!
        )
    }
}
