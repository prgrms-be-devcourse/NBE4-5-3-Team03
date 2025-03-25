package com.example.Flicktionary.domain.series.dto;

import com.example.Flicktionary.domain.director.dto.DirectorDto;
import com.example.Flicktionary.domain.genre.dto.GenreDto;
import com.example.Flicktionary.domain.series.entity.Series;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Getter
public class SeriesDetailResponse {

    private Long id;

    private Long tmdbId;

    private String title;

    private String posterPath;

    private double averageRating;

    private int ratingCount;

    private Integer episode;

    private String plot;

    private String company;

    private String nation;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String status;

    private List<GenreDto> genres;

    private List<SeriesCastDto> casts;

    private DirectorDto director;

    public SeriesDetailResponse(Series series) {
        this.id = series.getId();
        this.tmdbId = series.getTmdbId();
        this.title = series.getTitle();
        this.posterPath = series.getPosterPath();
        this.averageRating = series.getAverageRating();
        this.ratingCount = series.getRatingCount();
        this.episode = series.getEpisodeNumber();
        this.plot = series.getOverview();
        this.company = series.getProductionCompany();
        this.nation = series.getProductionCountry();
        this.releaseStartDate = series.getReleaseStartDate();
        this.releaseEndDate = series.getReleaseEndDate();
        this.status = series.getStatus();

        this.genres = Optional.ofNullable(series.getGenres())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(GenreDto::new)
                .toList();

        this.casts = Optional.ofNullable(series.getCasts())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(SeriesCastDto::new)
                .toList();

        this.director = series.getDirector() != null ? new DirectorDto(series.getDirector()) : null;
    }
}
