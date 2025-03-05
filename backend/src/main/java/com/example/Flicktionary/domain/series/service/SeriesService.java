package com.example.Flicktionary.domain.series.service;

import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.series.dto.SeriesDetailDto;
import com.example.Flicktionary.domain.series.dto.SeriesPopularIdDto;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbPopularSeriesResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    private final GenreRepository genreRepository;

    private final RestTemplate restTemplate;

    @Value("${tmdb.access-token}")
    private String accessToken;

    @Value("${tmdb.base-image-url}")
    private String baseImageUrl;

    // 인기도 순으로 DB에 저장(페이지당 20개)
    @PostConstruct
    public void fetchDataOnStartup() {
        int startPage = 1; // 시작 페이지
        int endPage = 1;   // 끝 페이지

        // 페이지 번호에 따라 반복
        for (int page = startPage; page <= endPage; page++) {
            String url = String.format("https://api.themoviedb.org/3/tv/popular?language=ko-KR&page=%d", page);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", accessToken);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<TmdbPopularSeriesResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        TmdbPopularSeriesResponse.class
                );

                if (response.getBody() == null) {
                    throw new RuntimeException("TMDB API 응답이 null입니다.");
                }

                for (SeriesPopularIdDto dto : response.getBody().getResults()) {
                    fetchAndSaveSeriesDetails(dto.getId());
                }


            } catch (Exception e) {
                throw new RuntimeException("TMDB API 요청 실패1: " + e.getMessage());
            }
        }
    }

    // 각 시리즈의 상세 정보를 가져와서 DB에 저장
    private void fetchAndSaveSeriesDetails(Long seriesId) throws InterruptedException {
        Thread.sleep(100);
        String url = String.format("https://api.themoviedb.org/3/tv/%d?language=ko-KR", seriesId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SeriesDetailDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    SeriesDetailDto.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("TMDB API 응답이 null입니다.");
            }

            //Genre 엔티티 생성
            List<Genre> genres = response.getBody().getGenres().stream()
                    .map(genreDto ->
                            genreRepository.findByName(genreDto.getName())
                                    .orElseGet(() -> {
                                        // 만약 DB에 없으면 새로 생성하여 저장
                                        Genre newGenre = new Genre(genreDto.getName());
                                        genreRepository.save(newGenre);
                                        return newGenre;  // 새로 생성된 장르를 반환
                                    })
                    )
                    .collect(Collectors.toList());

            // Series 엔티티 생성
            Series series = SeriesDetailDto.toEntity(response, genres, baseImageUrl);

            seriesRepository.save(series);
        } catch (Exception e) {
            throw new RuntimeException("TMDB API 요청 실패2: " + e.getMessage());
        }
    }
}
