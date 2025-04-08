package com.example.Flicktionary.domain.series.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.entity.SeriesCast
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.tmdb.service.TmdbService
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SeriesService(
    private val seriesRepository: SeriesRepository,
    private val genreRepository: GenreRepository,
    private val actorRepository: ActorRepository,
    private val directorRepository: DirectorRepository,
    private val tmdbService: TmdbService
) {
    private val BASE_IMAGE_URL = "https://image.tmdb.org/t/p"

    //Series 목록 조회(검색, 페이징, 정렬)
    @Transactional(readOnly = true)
    fun getSeries(keyword: String, page: Int, pageSize: Int, sortBy: String): Page<Series> {
        val sort = getSort(sortBy)

        if (page < 1) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value(), "페이지는 1부터 요청 가능합니다.")
        }

        val pageable: Pageable = PageRequest.of(page - 1, pageSize, sort)
        return seriesRepository.findByTitleLike(keyword, pageable)
    }

    private fun getSort(sortBy: String): Sort {
        return when (sortBy) {
            "id" -> Sort.by(Sort.Direction.ASC, "id")
            "rating" -> Sort.by(Sort.Direction.DESC, "averageRating")
            "ratingCount" -> Sort.by(Sort.Direction.DESC, "ratingCount")
            else -> throw ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬 기준입니다.")
        }
    }

    //Series 상세 조회
    @Transactional(readOnly = true)
    fun getSeriesDetail(id: Long): SeriesDetailResponse {
        val series: Series = seriesRepository.findByIdWithCastsAndDirector(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 시리즈를 찾을 수 없습니다.")

        return SeriesDetailResponse(series)
    }

    @Transactional
    fun fetchAndSaveSeries(pages: Int) {
        val seriesToSave: MutableList<Series> = mutableListOf()
        val genreCache: MutableMap<Long, Genre> = mutableMapOf()
        val actorCache: MutableMap<Long, Actor> = mutableMapOf()
        val directorCache: MutableMap<Long, Director> = mutableMapOf()

        for (i in 1..pages) {
            val seriesDtos = tmdbService.fetchSeries(i)

            // 기존 시리즈 ID를 미리 조회하여 중복 방지
            val existingSeriesIds = seriesRepository.findAllTmdbIds()

            for (seriesDto in seriesDtos) {
                if (existingSeriesIds.contains(seriesDto.tmdbId) ||
                    seriesToSave.stream().anyMatch { s: Series -> s.tmdbId == seriesDto.tmdbId }
                ) {
                    continue  // 이미 존재하는 시리즈면 스킵
                }

                val series = Series(
                    seriesDto.tmdbId,
                    seriesDto.title,
                    seriesDto.overview,
                    if (seriesDto.releaseStartDate == null || seriesDto.releaseStartDate.isEmpty())
                        null
                    else
                        LocalDate.parse(seriesDto.releaseStartDate),
                    if (seriesDto.releaseEndDate == null || seriesDto.releaseEndDate.isEmpty())
                        null
                    else
                        LocalDate.parse(seriesDto.releaseEndDate),
                    seriesDto.status,
                    seriesDto.posterPath?.let { "$BASE_IMAGE_URL/w342$it" },
                    seriesDto.numberOfEpisodes,
                    if (seriesDto.productionCountries.isEmpty()) null else seriesDto.productionCountries[0].name,
                    if (seriesDto.productionCompanies.isEmpty()) null else seriesDto.productionCompanies[0].name
                )

                // 장르 저장 (캐싱 활용)
                for (tmdbGenre in seriesDto.genres) {
                    val genre = genreCache.computeIfAbsent(tmdbGenre.id) { id: Long ->
                        genreRepository.findByIdOrNull(id)
                            ?: genreRepository.save(Genre(id, tmdbGenre.name))
                    }
                    series.genres.add(genre)
                }

                // 배우 저장 (캐싱 활용)
                for (tmdbActor in seriesDto.credits.cast.stream().limit(5).toList()) {
                    val actor = actorCache.computeIfAbsent(tmdbActor.id) { id: Long ->
                        actorRepository.findByIdOrNull(id)
                            ?: actorRepository.save(
                                Actor(
                                    id, tmdbActor.name,
                                    tmdbActor.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                                ))
                    }

                    val seriesCast = SeriesCast(series, actor, tmdbActor.character)
                    series.casts.add(seriesCast)
                }

                // 감독 저장 (캐싱 활용)
                for (crew in seriesDto.credits.crew) {
                    if (crew.job.equals("Director", ignoreCase = true)) {
                        val director = directorCache.computeIfAbsent(crew.id) { id: Long ->
                            directorRepository.findByIdOrNull(id)
                                ?: directorRepository.save(
                                    Director(
                                        id, crew.name,
                                        crew.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
                                    )
                                )
                        }
                        series.director = director

                        if (!director.series.contains(series)) {
                            director.series.add(series)
                        }
                    }
                }

                seriesToSave.add(series)
            }
        }

        if (seriesToSave.isNotEmpty()) {
            seriesRepository.saveAll(seriesToSave)
        }
    }
}
