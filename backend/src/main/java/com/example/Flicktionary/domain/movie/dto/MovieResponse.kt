package com.example.Flicktionary.domain.movie.dto

import com.example.Flicktionary.domain.movie.entity.Movie

data class MovieResponse(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val averageRating: Double,
    val ratingCount: Int
) {
    constructor(movie: Movie) : this(
        movie.id,
        movie.title,
        movie.posterPath,
        movie.averageRating,
        movie.ratingCount
    )
}
