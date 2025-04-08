package com.example.Flicktionary.domain.director.service

import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class DirectorService(
    private val directorRepository: DirectorRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) {
    fun getDirectors(keyword: String, page: Int, pageSize: Int): Page<Director> {
        val pageable: Pageable = PageRequest.of(page - 1, pageSize)
        return directorRepository.findByNameLike(keyword, pageable)
    }

    fun getDirector(id: Long): Director {
        return directorRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 감독을 찾을 수 없습니다."
                )
            }
    }

    fun getMoviesByDirectorId(id: Long): List<Movie> {
        return movieRepository.findByDirectorId(id)
    }

    fun getSeriesByDirectorId(id: Long): List<Series> {
        return seriesRepository.findByDirectorId(id)
    }
}
