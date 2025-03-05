package com.example.Flicktionary.domain.tmdb.dto;

import com.example.Flicktionary.domain.series.dto.SeriesPopularIdDto;
import lombok.Getter;

import java.util.List;

@Getter
public class TmdbPopularSeriesResponse {
    private List<SeriesPopularIdDto> results;
}
