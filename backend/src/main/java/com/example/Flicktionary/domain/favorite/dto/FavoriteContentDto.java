package com.example.Flicktionary.domain.favorite.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteContentDto {
    private String title;
    private String imageUrl;
    private double averageRating;
    private int ratingCount;

    public static FavoriteContentDto fromMovie(Movie movie) {
        return FavoriteContentDto.builder()
                .title(movie.getTitle())
                .imageUrl(movie.getPosterPath())
                .averageRating(movie.getAverageRating())
                .ratingCount(movie.getRatingCount())
                .build();
    }

    public static FavoriteContentDto fromSeries(Series series) {
        return FavoriteContentDto.builder()
                .title(series.getTitle())
                .imageUrl(series.getPosterPath())
                .averageRating(series.getAverageRating())
                .ratingCount(series.getRatingCount())
                .build();
    }

}
