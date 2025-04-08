package com.example.Flicktionary.global.exception

import com.example.Flicktionary.global.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    // ServiceException 처리
    @ResponseStatus
    @ExceptionHandler(
        ServiceException::class
    )
    fun serviceExceptionHandler(e: ServiceException): ResponseEntity<ResponseDto<Void>> {
        return ResponseEntity
            .status(e.code)
            .body(ResponseDto.of(e.code.toString(), e.message, null))
    }

    // 그 외 모든 예외 처리
    @ResponseStatus
    @ExceptionHandler(
        Exception::class
    )
    fun exceptionHandler(e: Exception): ResponseEntity<ResponseDto<Void>> {
        val code = HttpStatus.INTERNAL_SERVER_ERROR.value()
        e.printStackTrace()
        return ResponseEntity
            .status(code)
            .body(ResponseDto.of(code.toString(), "서버 에러가 발생했습니다.", null))
    }
}
