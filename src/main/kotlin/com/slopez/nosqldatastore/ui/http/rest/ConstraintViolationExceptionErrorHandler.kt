package com.slopez.nosqldatastore.ui.http.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.IOException
import javax.validation.ConstraintViolationException


data class ErrorResponse(val message: String, val details: String)

@ControllerAdvice
class ConstraintViolationExceptionErrorHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    @Throws(IOException::class)
    fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<Any> {
        return ResponseEntity(ErrorResponse("Bad Request", exception.localizedMessage), HttpStatus.BAD_REQUEST)
    }
}

