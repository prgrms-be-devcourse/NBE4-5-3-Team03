package com.example.Flicktionary.domain.favorite.entity

enum class ContentType(val type: String) {
    MOVIE("영화"),
    SERIES("시리즈");

    private val description: String
    get() = when (this) {
        MOVIE -> "영화"
        SERIES -> "시리즈"
    }
}
