package com.example.Flicktionary.domain.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbSeriesResponseWithDetail(
        @JsonProperty("id") Long tmdbId,
        @JsonProperty("name")
        String title,
        String overview,
        @JsonProperty("first_air_date") String releaseStartDate,
        @JsonProperty("last_air_date") String releaseEndDate,
        String status,
        @JsonProperty("poster_path") String posterPath,
        Integer runtime,
        @JsonProperty("number_of_episodes") Integer numberOfEpisodes,
        @JsonProperty("production_countries") List<TmdbProductionCountry> productionCountries,
        @JsonProperty("production_companies") List<TmdbProductionCompany> productionCompanies,
        List<TmdbGenre> genres,
        TmdbCredits credits
) {
    public record TmdbProductionCountry(String name) {
    }

    public record TmdbProductionCompany(String name) {
    }

    public record TmdbGenre(Long id, String name) {
    }

    public record TmdbCredits(List<TmdbActor> cast, List<TmdbCrew> crew) {
    }

    public record TmdbActor(Long id, String name, @JsonProperty("profile_path") String profilePath, String character) {
    }

    public record TmdbCrew(Long id, String name, @JsonProperty("profile_path") String profilePath, String job) {
    }
}
