package com.example.Flicktionary.domain.director.service

import com.example.Flicktionary.domain.director.dto.DirectorRequest
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DirectorService(
    private val directorRepository: DirectorRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) {
    @Transactional(readOnly = true)
    fun getDirectors(keyword: String, page: Int, pageSize: Int): Page<Director> {
        val pageable: Pageable = PageRequest.of(page - 1, pageSize)
        val formattedKeyword = keyword.lowercase().replace(" ", "")
        return directorRepository.findByNameLike(formattedKeyword, pageable)
    }

    @Transactional(readOnly = true)
    fun getDirector(id: Long): Director {
        return directorRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 감독을 찾을 수 없습니다."
                )
            }
    }

    @Transactional(readOnly = true)
    fun getMoviesByDirectorId(id: Long): List<Movie> {
        return movieRepository.findByDirectorId(id)
    }

    @Transactional(readOnly = true)
    fun getSeriesByDirectorId(id: Long): List<Series> {
        return seriesRepository.findByDirectorId(id)
    }

    @Transactional
    fun createDirector(request: DirectorRequest): Director {
        if (directorRepository.existsDirectorByNameAndProfilePath(request.name, request.profilePath)) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 감독입니다.")
        }

        val director = Director(name = request.name, profilePath = request.profilePath)
        return directorRepository.save(director)
    }

    @Transactional
    fun updateDirector(id: Long, request: DirectorRequest): Director {
        val director = directorRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 감독을 찾을 수 없습니다.")

        director.name = request.name
        director.profilePath = request.profilePath

        return director
    }

    @Transactional
    fun deleteDirector(id: Long) {
        val director = directorRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 감독을 찾을 수 없습니다.")

        directorRepository.delete(director)
    }


}
