package com.example.Flicktionary.domain.tmdb.service;

import com.example.Flicktionary.domain.movie.dto.MovieDto;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMovieResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMoviesResponse;
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

    // 인기 영화 목록에서 영화를 가져옵니다.
    public List<MovieDto> fetchMovies(int page) {
        String url = "/movie/popular?language=ko-KR&page=" + page;
        return getMovies(url);
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

    // 영화 검색 결과를 가져옵니다.
    public List<MovieDto> searchMovies(String keyword) {
        String url = "/search/movie?query=%s&language=ko-KR".formatted(keyword);
        return getMovies(url);
    }

    private List<MovieDto> getMovies(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            TmdbMoviesResponse response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(TmdbMoviesResponse.class);

            if (response == null) {
                throw new RuntimeException("TMDB API 응답 내용이 없습니다.");
            }

            return response.getResults();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage());
        }
    }
}
