package com.back.global.globalExceptionHandler

import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handle(ex: NoSuchElementException): ResponseEntity<RsData<Void?>> {
        val message = ex.message ?: "해당 데이터가 존재하지 않습니다."

        return ResponseEntity(
            RsData("404-1", message),
            NOT_FOUND
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(ex: ConstraintViolationException): ResponseEntity<RsData<Void?>> {
        val message = ex.constraintViolations
            .map{ violation: ConstraintViolation<*> ->
                val field: String =
                    violation.propertyPath.toString().split("\\.".toRegex(), limit = 2).toTypedArray()[1]
                val messageTemplateBits: Array<String> =
                    violation.messageTemplate.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val code = messageTemplateBits[messageTemplateBits.size - 2]
                val message: String = violation.message
                "${field}-${code}-${message}"
            }
            .sorted()
            .joinToString("\n")

        return ResponseEntity(
            RsData("400-1", message),
            BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<RsData<Void?>> {
        val message = ex.bindingResult
            .allErrors
            .asSequence()
            .filterIsInstance<FieldError>()
            .map { error: ObjectError -> error as FieldError }
            .map { error: FieldError -> error.field + "-" + error.code + "-" + error.defaultMessage }
            .sorted()
            .joinToString("\n")

        return ResponseEntity(
            RsData("400-1", message),
            BAD_REQUEST
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(ex: HttpMessageNotReadableException): ResponseEntity<RsData<Void?>> {
        return ResponseEntity(
            RsData("400-2", "요청 본문이 올바르지 않습니다."),
            BAD_REQUEST
        )
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handle(ex: MissingRequestHeaderException): ResponseEntity<RsData<Void?>> {
        return ResponseEntity(
            RsData("400-2", "헤더에 ${ex.headerName}이 필요합니다."),
            BAD_REQUEST
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handle(ex: NoResourceFoundException?): ResponseEntity<RsData<Void?>> {
        return ResponseEntity(
            RsData("404-1", "리소스를 찾을 수 없습니다."),
            NOT_FOUND
        )
    }

    @ExceptionHandler(ServiceException::class)
    fun handle(
        ex: ServiceException,
        response: HttpServletResponse
    ): RsData<Void> {
        val rsData: RsData<Void> = ex.rsData
        response.status = rsData.statusCode
        return rsData
    }

    @ExceptionHandler(java.lang.Exception::class)
    fun handle(ex: java.lang.Exception?): ResponseEntity<RsData<Void?>> {
        return ResponseEntity(
            RsData("500-1", "일시적인 장애가 발생했습니다."),
            INTERNAL_SERVER_ERROR
        )
    }
}