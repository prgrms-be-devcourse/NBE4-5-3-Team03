package com.example.Flicktionary.domain.genre.dto

import jakarta.validation.constraints.NotBlank

data class GenreRequest(
    @NotBlank(message = "장르 이름은 필수입니다.")
    val name: String
)