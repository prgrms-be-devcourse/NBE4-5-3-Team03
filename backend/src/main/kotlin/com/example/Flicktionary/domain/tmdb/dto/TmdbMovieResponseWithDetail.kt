package com.example.Flicktionary.domain.tmdb.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TmdbMovieResponseWithDetail(
    @field:JsonProperty("id") @param:JsonProperty("id") val tmdbId: Long,
    val title: String,
    val overview: String,
    @field:JsonProperty("release_date") @param:JsonProperty("release_date") val releaseDate: String,
    val status: String,
    @field:JsonProperty("poster_path") @param:JsonProperty("poster_path") val posterPath: String?,
    val runtime: Int,
    @field:JsonProperty("production_countries") @param:JsonProperty("production_countries") val productionCountries: List<TmdbProductionCountry>,
    @field:JsonProperty("production_companies") @param:JsonProperty("production_companies") val productionCompanies: List<TmdbProductionCompany>,
    val genres: List<TmdbGenre>,
    val credits: TmdbCredits
) {
    data class TmdbProductionCountry(val name: String)

    data class TmdbProductionCompany(val name: String)

    data class TmdbGenre(val id: Long, val name: String)

    data class TmdbCredits(val cast: List<TmdbActor>, val crew: List<TmdbCrew>)

    data class TmdbActor(
        val id: Long, val name: String,
        @field:JsonProperty("profile_path") @param:JsonProperty("profile_path") val profilePath: String?,
        val character: String
    )

    data class TmdbCrew(
        val id: Long, val name: String,
        @field:JsonProperty("profile_path") @param:JsonProperty("profile_path") val profilePath: String?,
        val job: String
    )
}
