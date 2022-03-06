package com.slopez.nosqldatastore.ui.http.rest

import com.slopez.nosqldatastore.application.service.StringCannotBeRepresentedAsIntegerException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.IOException
import javax.validation.ConstraintViolationException


data class ErrorResponse(val message: String, val details: String)

@ControllerAdvice
class CustomExceptionErrorHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    @Throws(IOException::class)
    internal fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<Any> {
        return ResponseEntity(ErrorResponse("Bad Request", exception.localizedMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @Throws(IOException::class)
    internal fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<Any> {
        val errors: MutableList<String> = ArrayList()
        for (error in exception.bindingResult.fieldErrors) {
            errors.add(error.field + ": " + error.defaultMessage)
        }
        return ResponseEntity(ErrorResponse("Bad Request", errors.toString()), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(StringCannotBeRepresentedAsIntegerException::class)
    @Throws(IOException::class)
    internal fun handleStringCannotBeRepresentedAsIntegerException(exception: StringCannotBeRepresentedAsIntegerException): ResponseEntity<Any> {
        return ResponseEntity(ErrorResponse("Domain conflict: ", exception.localizedMessage), HttpStatus.CONFLICT)
    }
}

