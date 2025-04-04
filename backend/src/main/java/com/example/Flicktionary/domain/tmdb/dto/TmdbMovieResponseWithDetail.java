package com.example.Flicktionary.domain.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieResponseWithDetail(
        @JsonProperty("id") Long tmdbId,
        String title,
        String overview,
        @JsonProperty("release_date") String releaseDate,
        String status,
        @JsonProperty("poster_path") String posterPath,
        Integer runtime,
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
