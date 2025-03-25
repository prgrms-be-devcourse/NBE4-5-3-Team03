package com.example.Flicktionary.domain.tmdb.service;

import com.example.Flicktionary.domain.tmdb.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TmdbService {
    @Value("${tmdb.access-token}")
    private String accessToken;
    private final RestClient restClient;

    public TmdbService(RestClient.Builder builder) {
        String BASE_URL = "https://api.themoviedb.org/3";
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    // tmdb api를 이용해서 인기 영화를 가져옵니다.
    public List<TmdbMovieResponseWithDetail> fetchMovies(int page) {
        String url = "/movie/popular?language=ko-KR&page=" + page;
        List<Long> movieIds = getMovies(url);

        return movieIds.stream().map(this::fetchMovie).toList();
    }

    private List<Long> getMovies(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            TmdbMoviesResponse response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbMoviesResponse.class);

            if (response == null || response.getResults() == null) {
                throw new RuntimeException("TMDB API 응답 내용이 없습니다.");
            }

            return response.getResults().stream().map(TmdbMoviesIdResponse::getId).toList();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }

    // 영화 상세 정보를 가져옵니다.
    public TmdbMovieResponseWithDetail fetchMovie(long tmdbId) {
        String url = "/movie/%d?language=ko-KR&append_to_response=credits".formatted(tmdbId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            return restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbMovieResponseWithDetail.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }

    // tmdb api를 이용해서 인기 시리즈를 가져옵니다.
    public List<TmdbSeriesResponseWithDetail> fetchSeries(int page) {
        String url = "/tv/popular?language=ko-KR&page=" + page;
        List<Long> seriesIds = getSeries(url);

        return seriesIds.stream().map(this::fetchSeries).toList();
    }

    private List<Long> getSeries(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            TmdbSeriesResponse response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbSeriesResponse.class);

            if (response == null || response.getResults() == null) {
                throw new RuntimeException("TMDB API 응답 내용이 없습니다.");
            }

            return response.getResults().stream().map(TmdbSeriesIdResponse::getId).toList();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }

    // 시리즈 상세 정보를 가져옵니다.
    public TmdbSeriesResponseWithDetail fetchSeries(long tmdbId) {
        String url = "/tv/%d?language=ko-KR&append_to_response=credits".formatted(tmdbId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            return restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbSeriesResponseWithDetail.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }

}
