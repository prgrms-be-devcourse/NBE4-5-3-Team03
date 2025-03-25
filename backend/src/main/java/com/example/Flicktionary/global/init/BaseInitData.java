package com.example.Flicktionary.global.init;

import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.series.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final SeriesService seriesService;
    private final SeriesRepository seriesRepository;

    @Bean
    public ApplicationRunner applicationRunner1() {
        return args -> {
            if (movieRepository.count() > 0) {
                return;
            }

            movieService.fetchAndSaveMovies(5);
        };
    }

    @Bean
    public ApplicationRunner applicationRunner2() {
        return args -> {
            if (seriesRepository.count() > 0) {
                return;
            }

            seriesService.fetchAndSaveSeries(5);
        };
    }


}
