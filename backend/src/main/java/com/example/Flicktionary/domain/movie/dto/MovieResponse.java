package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.movie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
public class MovieResponse {
    @NonNull
    private final Long id;
    @NonNull
    private final String title;
    private final String posterPath;
    @NonNull
    private final double averageRating;
    @NonNull
    private final int ratingCount;

    public MovieResponse(Movie movie) {
        this(movie.getId(), movie.getTitle(), movie.getPosterPath(), movie.getAverageRating(), movie.getRatingCount());
    }
}
