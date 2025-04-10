package com.example.Flicktionary.domain.series.dto

import com.example.Flicktionary.domain.series.entity.Series

class SeriesSummaryResponse(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val averageRating: Double,
    val ratingCount: Int
) {
    constructor(series: Series) : this(
        series.id,
        series.title,
        series.posterPath,
        series.averageRating,
        series.ratingCount
    )
}
