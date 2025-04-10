package com.example.Flicktionary.domain.actor.dto

import jakarta.validation.constraints.NotBlank

data class ActorRequest(
    @NotBlank(message = "배우 이름은 필수입니다.")
    val name: String,
    val profilePath: String?
)
