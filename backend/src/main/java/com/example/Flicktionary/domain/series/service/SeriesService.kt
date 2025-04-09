package com.example.Flicktionary.domain.series.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse
import com.example.Flicktionary.domain.series.dto.SeriesRequest
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

    @Transactional
    fun createSeries(request: SeriesRequest): SeriesDetailResponse {
        val series = Series(
            title = request.title,
            overview = request.overview,
            releaseStartDate = request.releaseStartDate,
            releaseEndDate = request.releaseEndDate,
            status = request.status,
            posterPath = request.posterPath,
            episodeNumber = request.episodeNumber,
            productionCountry = request.productionCountry,
            productionCompany = request.productionCompany
        )

        // 장르 추가
        val genres = genreRepository.findAllById(request.genreIds)
        series.genres.addAll(genres)

        // 배우 추가
        val casts = request.casts.map {
            val actor = actorRepository.findByIdOrNull(it.actorId)
                ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${it.actorId}번 배우를 찾을 수 없습니다.")
            SeriesCast(series, actor, it.characterName)
        }
        series.casts.addAll(casts)

        // 감독 설정
        val director = directorRepository.findByIdOrNull(request.directorId)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${request.directorId}번 감독을 찾을 수 없습니다.")
        series.director = director

        val savedSeries = seriesRepository.save(series)
        return SeriesDetailResponse(savedSeries)
    }

    @Transactional
    fun updateSeries(id: Long, request: SeriesRequest): SeriesDetailResponse {
        val series = seriesRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 시리즈를 찾을 수 없습니다.")

        series.apply {
            title = request.title
            overview = request.overview
            releaseStartDate = request.releaseStartDate
            releaseEndDate = request.releaseEndDate
            status = request.status
            posterPath = request.posterPath
            episodeNumber = request.episodeNumber
            productionCountry = request.productionCountry
            productionCompany = request.productionCompany
        }

        val genres = genreRepository.findAllById(request.genreIds)
        series.genres.clear()
        series.genres.addAll(genres)

        val casts = request.casts.map {
            val actor = actorRepository.findByIdOrNull(it.actorId)
                ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${it.actorId}번 배우를 찾을 수 없습니다.")
            SeriesCast(series, actor, it.characterName)
        }
        series.casts.clear()
        series.casts.addAll(casts)

        val director = directorRepository.findByIdOrNull(request.directorId)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${request.directorId}번 감독을 찾을 수 없습니다.")
        series.director = director

        return SeriesDetailResponse(series)
    }

    @Transactional
    fun deleteSeries(id: Long) {
        val series = seriesRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 시리즈를 찾을 수 없습니다.")
        seriesRepository.delete(series)
    }

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
        val existingSeriesKeys = mutableSetOf<Pair<String, LocalDate?>>()

        for (i in 1..pages) {
            val seriesDtos = tmdbService.fetchSeries(i)

            for (seriesDto in seriesDtos) {
                val releaseStartDate =
                    if (seriesDto.releaseStartDate.isEmpty()) null else LocalDate.parse(seriesDto.releaseStartDate)
                val seriesKey = seriesDto.title to releaseStartDate

                if (existingSeriesKeys.contains(seriesKey)) {
                    continue
                }
                existingSeriesKeys.add(seriesKey)

                val series = Series(
                    seriesDto.title,
                    seriesDto.overview,
                    releaseStartDate,
                    if (seriesDto.releaseEndDate.isEmpty()) null else LocalDate.parse(seriesDto.releaseEndDate),
                    seriesDto.status,
                    seriesDto.posterPath?.let { "$BASE_IMAGE_URL/w342$it" },
                    seriesDto.numberOfEpisodes,
                    seriesDto.productionCountries.firstOrNull()?.name ?: "",
                    seriesDto.productionCompanies.firstOrNull()?.name ?: ""
                )

                // 장르 저장
                for (tmdbGenre in seriesDto.genres) {
                    val genre = genreRepository.findByIdOrNull(tmdbGenre.id)
                        ?: genreRepository.save(Genre(tmdbGenre.id, tmdbGenre.name))
                    series.genres.add(genre)
                }

                // 배우 저장
                for (tmdbActor in seriesDto.credits.cast.take(5)) {
                    val profilePath = tmdbActor.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                    if (actorRepository.existsByNameAndProfilePath(tmdbActor.name, profilePath)) {
                        continue
                    }
                    val actor = actorRepository.save(Actor(tmdbActor.name, profilePath))
                    series.casts.add(SeriesCast(series, actor, tmdbActor.character))
                }

                // 감독 저장
                seriesDto.credits.crew.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.let { crew ->
                    val director = directorRepository.findByIdOrNull(crew.id)
                        ?: directorRepository.save(
                            Director(crew.id, crew.name, crew.profilePath?.let { "$BASE_IMAGE_URL/w185$it" })
                        )
                    series.director = director
                    if (!director.series.contains(series)) {
                        director.series.add(series)
                    }
                }

                seriesToSave.add(series)
            }
        }

        seriesRepository.saveAll(seriesToSave)
    }
}
