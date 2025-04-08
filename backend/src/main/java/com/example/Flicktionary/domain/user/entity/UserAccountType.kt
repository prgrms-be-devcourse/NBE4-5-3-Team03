package com.example.Flicktionary.domain.user.entity


/**
 * 유저 계정의 분류를 나타내는 열거형.
 */
enum class UserAccountType(private val description: String) {
    USER("USER"),
    ADMIN("ADMIN")
}