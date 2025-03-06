package com.example.Flicktionary.domain.series.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
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

    private String title;

    private String imageUrl;

    private double avgRating;

    private int ratingCount;

    private int episode;

    private String plot;

    private String company;

    private String nation;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String status;

    private List<GenreDto> genres;

    private List <ActorDto> actors;

    private DirectorDto director;

    public SeriesDetailResponse(Series series) {
        this.id = series.getId();
        this.title = series.getTitle();
        this.imageUrl = series.getImageUrl();
        this.avgRating = series.getAvgRating();
        this.ratingCount = series.getRatingCount();
        this.episode = series.getEpisode();
        this.plot = series.getPlot();
        this.company = series.getCompany();
        this.nation = series.getNation();
        this.releaseStartDate = series.getReleaseStartDate();
        this.releaseEndDate = series.getReleaseEndDate();
        this.status = series.getStatus();

        this.genres = Optional.ofNullable(series.getGenres())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(GenreDto::new)
                .toList();

        this.actors = Optional.ofNullable(series.getActors())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ActorDto::new)
                .toList();

        this.director = series.getDirector() != null ? new DirectorDto(series.getDirector()) : null;
    }
}
