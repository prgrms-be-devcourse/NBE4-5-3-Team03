package com.example.Flicktionary.domain.movie.dto

import com.example.Flicktionary.domain.director.dto.DirectorDto
import com.example.Flicktionary.domain.genre.dto.GenreDto
import com.example.Flicktionary.domain.movie.entity.Movie
import java.time.LocalDate

data class MovieResponseWithDetail(
    val id: Long,
    val title: String,
    val overview: String,
    val releaseDate: LocalDate?,
    val posterPath: String?,
    val status: String,
    val runtime: Int,
    val productionCountry: String?,
    val productionCompany: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val genres: List<GenreDto>,
    val casts: List<MovieCastDto>,
    val director: DirectorDto?
) {
    constructor(movie: Movie) : this(
        id = movie.id,
        title = movie.title,
        overview = movie.overview,
        releaseDate = movie.releaseDate,
        posterPath = movie.posterPath,
        status = movie.status,
        runtime = movie.runtime,
        productionCountry = movie.productionCountry,
        productionCompany = movie.productionCompany,
        averageRating = movie.averageRating,
        ratingCount = movie.ratingCount,
        genres = movie.genres.map { GenreDto(it.id, it.name) },
        casts = movie.casts.map { MovieCastDto(it) },
        director = movie.director?.let { DirectorDto(it) }
    )
}
