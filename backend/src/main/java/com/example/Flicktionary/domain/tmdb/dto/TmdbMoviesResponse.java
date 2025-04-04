package com.example.Flicktionary.domain.tmdb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TmdbMoviesResponse {
    private List<TmdbMoviesIdResponse> results;
}
