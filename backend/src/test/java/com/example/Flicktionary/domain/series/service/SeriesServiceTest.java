package com.example.Flicktionary.domain.series.service;

import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("시리즈 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class SeriesServiceTest {

    @Mock
    private SeriesRepository seriesRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private ActorRepository actorRepository;
    @Mock
    private DirectorRepository directorRepository;
    @Mock
    private TmdbService tmdbService;

    @InjectMocks
    SeriesService seriesService;

    private Series series;

    @BeforeEach
    void setUp() {
        series = new Series("testTitle", "",
                LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
                "", "series.png", 10, "", "");
        series.setId(123L);
    }

    @Test
    @DisplayName("Series 목록 조회 - id 오름차순 정렬")
    void getSeriesSortByIdTest() {
        String keyword = "", sortBy = "id";
        int page = 1, pageSize = 10;
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(seriesRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(series),
                        PageRequest.of(page - 1, pageSize, Sort.by("id").ascending()),
                        10));

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(series).isNotNull();
        assertThat(series.getContent().size()).isGreaterThan(0);
        assertEquals(123L, series.getContent().getFirst().getId());
        assertEquals("testTitle", series.getContent().getFirst().getTitle());
        assertEquals(Sort.by("id").ascending(), captured.getSort());
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());
        then(seriesRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Series 목록 조회 - 평점 내림차순 정렬")
    void getSeriesSortByRatingTest() {
        String keyword = "", sortBy = "rating";
        int page = 1, pageSize = 10;
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(seriesRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(series),
                        PageRequest.of(page - 1, pageSize, Sort.by("averageRating").descending()),
                        10));

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(series).isNotNull();
        assertThat(series.getContent().size()).isGreaterThan(0);
        assertEquals(Sort.by("averageRating").descending(), captured.getSort());
        then(seriesRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Series 목록 조회 - 리뷰 개수 내림차순 정렬")
    void getSeriesSortByRatingCountTest() {
        String keyword = "", sortBy = "ratingCount";
        int page = 1, pageSize = 10;
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(seriesRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(series),
                        PageRequest.of(page - 1, pageSize, Sort.by("ratingCount").descending()),
                        10));

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(series).isNotNull();
        assertEquals(Sort.by("ratingCount").descending(), captured.getSort());
        then(seriesRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Series 목록 조회 - 검색")
    void getSeriesForSearchTest() {
        String keyword = "The", sortBy = "id";
        int page = 1, pageSize = 10;
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        given(seriesRepository.findByTitleLike(captor.capture(), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        List.of(series),
                        PageRequest.of(page - 1, pageSize, Sort.by("id").ascending()),
                        10));

        Page<Series> series = seriesService.getSeries(keyword, page, pageSize, sortBy);
        String capturedString = captor.getValue();

        assertThat(series).isNotNull();
        assertEquals(keyword, capturedString);
        then(seriesRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Series 목록 조회 - 잘못된 정렬 방식 예외 처리")
    void getSeriesSortByFailTest() {
        String keyword = "The", sortBy = "invalidSortParameter";
        int page = 1, pageSize = 10;

        Throwable thrown = catchThrowable(() ->
                seriesService.getSeries(keyword, page, pageSize, sortBy));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("잘못된 정렬 기준입니다.");
    }

    // TODO: 서비스 메소드 시그니처 변경에 따라 테스트 수정
    @Test
    @DisplayName("Series 상세 정보 조회 성공 테스트")
    void testGetSeriesDetail_Success() {
        // given
        Long seriesId = 123L;
        given(seriesRepository.findByIdWithCastsAndDirector(seriesId))
                .willReturn(series);

        // when
        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId);

        // then
        assertNotNull(response);
        assertEquals(seriesId, response.getId());
        assertEquals("testTitle", response.getTitle());
        then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId);
    }

    @Test
    @DisplayName("Series 상세 정보 조회 실패 테스트 (존재하지 않는 ID)")
    void testGetSeriesDetail_Fail_NotFound() {
        // given
        Long seriesId = 999L;  // 존재하지 않는 ID
        given(seriesRepository.findByIdWithCastsAndDirector(seriesId)).willReturn(null);

        // when
        Throwable thrown = catchThrowable(() -> seriesService.getSeriesDetail(seriesId));

        // then
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 시리즈를 찾을 수 없습니다.".formatted(seriesId));
        then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId);
    }
}
