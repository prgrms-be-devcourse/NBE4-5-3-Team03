package com.example.Flicktionary.domain.tmdb.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TmdbSeriesResponse {
    private List<TmdbSeriesIdResponse> results;
}
