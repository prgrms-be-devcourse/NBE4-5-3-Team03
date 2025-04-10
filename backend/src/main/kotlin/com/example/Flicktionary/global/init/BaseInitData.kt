package com.example.Flicktionary.global.init

import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.movie.service.MovieService
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.series.service.SeriesService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BaseInitData(
    private val movieService: MovieService,
    private val movieRepository: MovieRepository,
    private val seriesService: SeriesService,
    private val seriesRepository: SeriesRepository
) {
    @Bean
    fun applicationRunner1(): ApplicationRunner {
        return ApplicationRunner {
            if (movieRepository.count() > 0) {
                return@ApplicationRunner
            }
            movieService.fetchAndSaveMovies(5)
        }
    }

    @Bean
    fun applicationRunner2(): ApplicationRunner {
        return ApplicationRunner {
            if (seriesRepository.count() > 0) {
                return@ApplicationRunner
            }
            seriesService.fetchAndSaveSeries(5)
        }
    }
}
