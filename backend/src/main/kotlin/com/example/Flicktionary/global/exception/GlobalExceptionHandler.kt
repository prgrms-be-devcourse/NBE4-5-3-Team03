package com.example.Flicktionary.global.exception

import com.example.Flicktionary.global.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
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
    fun serviceExceptionHandler(e: ServiceException): ResponseEntity<ResponseDto<Nothing>> {
        return ResponseEntity
            .status(e.code)
            .body(ResponseDto.of(e.code.toString(), e.message ?: ""))
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExcetionHandler(e: MethodArgumentNotValidException): ResponseEntity<ResponseDto<Nothing>> {
        val fieldError = e.bindingResult.fieldErrors.firstOrNull()
        val errorMessage = fieldError?.defaultMessage ?: "유효하지 않은 요청입니다."

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseDto.of(HttpStatus.BAD_REQUEST.toString(), errorMessage))
    }

    // 그 외 모든 예외 처리
    @ResponseStatus
    @ExceptionHandler(
        Exception::class
    )
    fun exceptionHandler(e: Exception): ResponseEntity<ResponseDto<Nothing>> {
        val code = HttpStatus.INTERNAL_SERVER_ERROR.value()
        e.printStackTrace()
        return ResponseEntity
            .status(code)
            .body(ResponseDto.of(code.toString(), "서버 에러가 발생했습니다."))
    }
}
