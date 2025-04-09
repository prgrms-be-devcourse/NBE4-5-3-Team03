package com.example.Flicktionary.domain.movie.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class MovieRequest(
    @field:NotBlank(message = "영화 제목은 필수입니다.")
    val title: String,

    @field:NotBlank(message = "줄거리는 필수입니다.")
    val overview: String,

    val releaseDate: LocalDate?,

    val status: String,

    val posterPath: String?,

    @field:Min(1, message = "상영 시간은 1분 이상이어야 합니다.")
    val runtime: Int,

    val productionCountry: String,

    val productionCompany: String,

    val genreIds: List<Long>,

    val casts: List<MovieCastRequest>,

    val directorId: Long
) {
    data class MovieCastRequest(
        val actorId: Long,
        val characterName: String
    )
}

