package com.example.Flicktionary.domain.favorite.dto

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series

data class FavoriteContentDto(
    val title: String,
    val imageUrl: String?,
    val averageRating: Double,
    val ratingCount: Int
) {

    companion object {
        fun fromMovie(movie: Movie): FavoriteContentDto {
            return FavoriteContentDto(
                title = movie.title,
                imageUrl = movie.posterPath,
                averageRating = movie.averageRating,
                ratingCount = movie.ratingCount
            )
        }

        fun fromSeries(series: Series): FavoriteContentDto {
            return FavoriteContentDto(
                title = series.title,
                imageUrl = series.posterPath,
                averageRating = series.averageRating,
                ratingCount = series.ratingCount
            )
        }
    }
}
