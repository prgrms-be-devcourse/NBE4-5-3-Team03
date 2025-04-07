package com.example.Flicktionary.domain.tmdb.service

import com.example.Flicktionary.domain.tmdb.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class TmdbService(builder: RestClient.Builder) {
    @Value("\${tmdb.access-token}")
    private lateinit var accessToken: String
    private lateinit var restClient: RestClient

    init {
        val BASE_URL = "https://api.themoviedb.org/3"
        this.restClient = builder.baseUrl(BASE_URL).build()
    }

    // tmdb api를 이용해서 인기 영화를 가져옵니다.
    fun fetchMovies(page: Int): List<TmdbMovieResponseWithDetail> {
        val url = "/movie/popular?language=ko-KR&page=$page"
        val movieIds = getMovies(url)

        return movieIds.stream().map { tmdbId: Long -> this.fetchMovie(tmdbId) }.toList()
    }

    private fun getMovies(url: String): List<Long> {
        val headers = HttpHeaders()
        headers["Authorization"] = accessToken

        val response = restClient.get()
            .uri(url)
            .headers { h: HttpHeaders -> h.addAll(headers) }
            .retrieve()
            .body(TmdbMoviesResponse::class.java)

        return response?.results?.stream()?.map(TmdbMoviesIdResponse::id)?.toList()
            ?: throw RuntimeException("TMDB API 응답 내용이 없습니다.")
    }

    // 영화 상세 정보를 가져옵니다.
    fun fetchMovie(tmdbId: Long): TmdbMovieResponseWithDetail {
        val url = "/movie/${tmdbId}?language=ko-KR&append_to_response=credits"

        val headers = HttpHeaders()
        headers["Authorization"] = accessToken

        return restClient.get()
            .uri(url)
            .headers { h: HttpHeaders -> h.addAll(headers) }
            .retrieve()
            .body(TmdbMovieResponseWithDetail::class.java)
            ?: throw RuntimeException("TMDB API 응답 내용이 없습니다.")

    }

    // tmdb api를 이용해서 인기 시리즈를 가져옵니다.
    fun fetchSeries(page: Int): List<TmdbSeriesResponseWithDetail> {
        val url = "/tv/popular?language=ko-KR&page=$page"
        val seriesIds = getSeries(url)

        return seriesIds.stream().map { tmdbId: Long -> this.fetchSeries(tmdbId) }.toList()
    }

    private fun getSeries(url: String): List<Long> {
        val headers = HttpHeaders()
        headers["Authorization"] = accessToken

        val response = restClient.get()
            .uri(url)
            .headers { h: HttpHeaders -> h.addAll(headers) }
            .retrieve()
            .body(TmdbSeriesResponse::class.java)

        return response?.results?.stream()?.map(TmdbSeriesIdResponse::id)?.toList()
            ?: throw RuntimeException("TMDB API 응답 내용이 없습니다.")
    }

    // 시리즈 상세 정보를 가져옵니다.
    fun fetchSeries(tmdbId: Long): TmdbSeriesResponseWithDetail {
        val url = "/tv/${tmdbId}?language=ko-KR&append_to_response=credits"

        val headers = HttpHeaders()
        headers["Authorization"] = accessToken

        return restClient.get()
            .uri(url)
            .headers { h: HttpHeaders -> h.addAll(headers) }
            .retrieve()
            .body(TmdbSeriesResponseWithDetail::class.java)
            ?: throw RuntimeException("TMDB API 응답 내용이 없습니다.")
    }
}
