package com.example.Flicktionary.domain.actor.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieCastRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesCastRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ActorService(
    private val actorRepository: ActorRepository,
    private val movieCastRepository: MovieCastRepository,
    private val seriesCastRepository: SeriesCastRepository
) {
    // 특정 배우 조회 (출연 영화 포함)
    fun getActorById(id: Long): Actor {
        return actorRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 배우를 찾을 수 없습니다."
                )
            }
    }

    // 배우가 출연한 영화 리스트 조회
    fun getMoviesByActorId(actorId: Long): List<Movie> {
        return movieCastRepository.findMoviesByActorId(actorId)
            .map { it.movie }
            .distinct()
            .sortedByDescending { it.releaseDate } // tmdbId 기준으로 정렬, 최신 순 정렬
    }

    // 배우가 출연한 시리즈 리스트 조회
    fun getSeriesByActorId(actorId: Long): List<Series> {
        return seriesCastRepository.findSeriesByActorId(actorId)
            .map { it.series }
            .distinct()
            .sortedByDescending { it.releaseStartDate } // tmdbId 기준으로 정렬, 최신 순 정렬
    }

    // 배우 목록 조회
    fun getActors(keyword: String, page: Int, pageSize: Int): Page<Actor> {
        val pageable: Pageable = PageRequest.of(page - 1, pageSize)
        return actorRepository.findByNameLike(keyword, pageable)
    }
}
