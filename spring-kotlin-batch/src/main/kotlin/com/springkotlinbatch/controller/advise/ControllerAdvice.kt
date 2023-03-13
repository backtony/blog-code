package com.springkotlinbatch.controller.advise

import com.springkotlinbatch.common.exeception.ResourceNotFoundException
import com.springkotlinbatch.common.exeception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerAdvice {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [ResourceNotFoundException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleResourceNotFoundException(e: ResourceNotFoundException, request: HttpServletRequest): ErrorResponse {
        logger.error("occurred error!: ${e.message}")
        return ErrorResponse(
            status = HttpStatus.NOT_FOUND.toString(),
            error = e.javaClass.simpleName,
            message = e.message!!,
            path = request.requestURI,
        )
    }
}
