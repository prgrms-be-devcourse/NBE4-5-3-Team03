package com.example.Flicktionary.global.dto

import org.springframework.http.HttpStatus

/**
 * HTTP 응답 DTO.
 * @param code 응답 코드
 * @param message 응답 메시지
 * @param data 응답 본문
 * @param <T> 본문의 타입
*/
data class ResponseDto<T>(
    val code: String?,
    val message: String?,
    val data: T? = null
) {
    companion object {
        /**
         * 응답 본문이 없는 경우의 생성자 메소드.
         * @param code
         * @param message
         */
        fun of(code: String, message: String): ResponseDto<Void> {
            return ResponseDto(code, message, null as Void?)
        }

        /**
         * 생성자 메소드.
         * @param code
         * @param message
         * @param data
         * @return
         * @param <T>
        */
        fun <T> of(code: String, message: String, data: T?): ResponseDto<T> {
            return ResponseDto(code, message, data)
        }

        /**
         * HTTP 응답 코드 200 OK에 해당하는 응답을 반환한다.
         * @param data 응답 본문
         * @return
         * @param <T> 본문의 타입
        */
        fun <T> ok(message: String, data: T): ResponseDto<T> {
            return ResponseDto(HttpStatus.OK.value().toString(), message, data)
        }

        fun <T> ok(data: T): ResponseDto<T> {
            return ResponseDto(HttpStatus.OK.value().toString(), "정상 처리되었습니다.", data)
        }

        fun ok(message: String): ResponseDto<Void> {
            return ResponseDto(HttpStatus.OK.value().toString(), message, null as Void?)
        }
    }
}