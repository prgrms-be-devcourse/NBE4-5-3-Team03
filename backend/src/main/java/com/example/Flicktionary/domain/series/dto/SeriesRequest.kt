package com.example.Flicktionary.domain.series.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class SeriesRequest(
    @field:NotBlank(message = "시리즈 제목은 필수입니다.")
    val title: String,

    @field:NotBlank(message = "줄거리는 필수입니다.")
    val overview: String,

    val releaseStartDate: LocalDate?,

    val releaseEndDate: LocalDate?,

    val status: String,

    val posterPath: String?,

    val episodeNumber: Int,

    val productionCountry: String,

    val productionCompany: String,

    val genreIds: List<Long>,

    val casts: List<SeriesCastRequest>,

    val directorId: Long
) {
    data class SeriesCastRequest(
        val actorId: Long,
        val characterName: String
    )
}
