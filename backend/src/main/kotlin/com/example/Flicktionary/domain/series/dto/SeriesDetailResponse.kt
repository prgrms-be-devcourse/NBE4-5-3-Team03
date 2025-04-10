package com.example.Flicktionary.domain.series.dto

import com.example.Flicktionary.domain.director.dto.DirectorDto
import com.example.Flicktionary.domain.genre.dto.GenreDto
import com.example.Flicktionary.domain.series.entity.Series
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import java.time.LocalDate

@AllArgsConstructor
@Builder
@Getter
class SeriesDetailResponse(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val episode: Int,
    val plot: String,
    val company: String,
    val nation: String,
    val releaseStartDate: LocalDate?,
    val releaseEndDate: LocalDate?,
    val status: String,
    val genres: List<GenreDto>,
    val casts: List<SeriesCastDto>,
    val director: DirectorDto?
) {
    constructor(series: Series) : this(
        series.id,
        series.title,
        series.posterPath,
        series.averageRating,
        series.ratingCount,
        series.episodeNumber,
        series.overview,
        series.productionCompany,
        series.productionCountry,
        series.releaseStartDate,
        series.releaseEndDate,
        series.status,
        genres = series.genres.map { GenreDto(it.id, it.name) },
        casts = series.casts.map { SeriesCastDto(it) },
        director = series.director?.let { DirectorDto(it) }
    )
}
