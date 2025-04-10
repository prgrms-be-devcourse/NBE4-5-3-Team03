package com.example.Flicktionary.domain.director.dto

import jakarta.validation.constraints.NotBlank

data class DirectorRequest(
    @NotBlank(message = "배우 이름은 필수입니다.")
    val name: String,
    val profilePath: String?
)
