package com.example.Flicktionary.domain.tmdb.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TmdbSeriesResponseWithDetail(
    @field:JsonProperty("id") @param:JsonProperty("id") val tmdbId: Long,
    @field:JsonProperty("name") @param:JsonProperty("name") val title: String,
    val overview: String,
    @field:JsonProperty("first_air_date") @param:JsonProperty("first_air_date") val releaseStartDate: String,
    @field:JsonProperty("last_air_date") @param:JsonProperty("last_air_date") val releaseEndDate: String,
    val status: String,
    @field:JsonProperty("poster_path") @param:JsonProperty("poster_path") val posterPath: String?,
    val runtime: Int,
    @field:JsonProperty("number_of_episodes") @param:JsonProperty("number_of_episodes") val numberOfEpisodes: Int,
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
