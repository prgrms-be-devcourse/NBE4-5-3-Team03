package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("영화 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TmdbService tmdbService;

    @InjectMocks
    private MovieService movieService;

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 기본")
    void getMovies1() {
        String keyword = "", sortBy = "id";
        int page = 1, pageSize = 10;
        // @Mock 애노테이션이 붙은 오브젝트의 메소드 호출시, Pageable 타입의 인수를 획득
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(movieRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(Movie.builder()
                                .id(123L)
                                .tmdbId(124L)
                                .title("testTitle")
                                .build()),
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                        10));

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getItems().size()).isGreaterThan(0);
        // getMovies내에서 생성된 Pageable 검증
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), captured.getSort());
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());
        // 반환된 PageDto 검증
        assertEquals(sortBy + ": ASC", result.getSortBy());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(page, result.getCurPageNo());
        then(movieRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 검색")
    void getMovies2() {
        String keyword = "해리", sortBy = "id";
        int page = 1, pageSize = 10;
        // @Mock 애노테이션이 붙은 오브젝트의 메소드 호출시, 하기 타입들의 인수를 획득
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        given(movieRepository.findByTitleLike(stringCaptor.capture(), pageableCaptor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(Movie.builder()
                                .id(123L)
                                .tmdbId(124L)
                                .title("testTitle")
                                .build()),
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                        10));

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);
        String capturedKeyword = stringCaptor.getValue();
        Pageable capturedPageable = pageableCaptor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getItems().size()).isGreaterThan(0);
        assertEquals(keyword, capturedKeyword);
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), capturedPageable.getSort());
        assertEquals(pageSize, capturedPageable.getPageSize());
        assertEquals(page - 1, capturedPageable.getPageNumber());
        assertEquals(sortBy + ": ASC", result.getSortBy());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(page, result.getCurPageNo());
        then(movieRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 평점 순 정렬")
    void getMovies3() {
        String keyword = "", sortBy = "rating";
        int page = 1, pageSize = 10;
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(movieRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(Movie.builder()
                                .id(123L)
                                .tmdbId(124L)
                                .title("testTitle")
                                .build()),
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "averageRating")),
                        10));

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getItems().size()).isGreaterThan(0);
        assertEquals(Sort.by(Sort.Direction.DESC, "averageRating"), captured.getSort());
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());
        assertEquals("averageRating: DESC", result.getSortBy());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(page, result.getCurPageNo());
        then(movieRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 리뷰수 순 정렬")
    void getMovies4() {
        String keyword = "", sortBy = "ratingCount";
        int page = 1, pageSize = 10;
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(movieRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(Movie.builder()
                                .id(123L)
                                .tmdbId(124L)
                                .title("testTitle")
                                .build()),
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "ratingCount")),
                        10));

        PageDto<MovieResponse> result = movieService.getMovies(keyword, page, pageSize, sortBy);
        Pageable captured = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getItems().size()).isGreaterThan(0);
        assertEquals(Sort.by(Sort.Direction.DESC, "ratingCount"), captured.getSort());
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(page - 1, captured.getPageNumber());
        assertEquals(sortBy + ": DESC", result.getSortBy());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(page, result.getCurPageNo());
        then(movieRepository).should().findByTitleLike(any(String.class), any(Pageable.class));
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies5() {
        String keyword = "", sortBy = "unknown";
        int page = 1, pageSize = 10;

        Throwable thrown = catchThrowable(() -> movieService.getMovies(keyword, page, pageSize, sortBy));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("잘못된 정렬 기준입니다.");
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공 - 기본")
    void getMovie1() {
        Movie testMovie = Movie.builder()
                .id(123L)
                .tmdbId(124L)
                .title("testTitle")
                .averageRating(1.23)
                .ratingCount(12)
                .status("Released")
                .build();
        given(movieRepository.findByIdWithCastsAndDirector(testMovie.getId()))
                .willReturn(Optional.of(testMovie));

        MovieResponseWithDetail result = movieService.getMovie(testMovie.getId());

        assertThat(result).isNotNull();
        assertEquals(testMovie.getId(), result.getId());
        assertEquals(testMovie.getTmdbId(), result.getTmdbId());
        assertEquals(testMovie.getAverageRating(), result.getAverageRating());
        assertEquals(testMovie.getRatingCount(), result.getRatingCount());
        then(movieRepository).should().findByIdWithCastsAndDirector(testMovie.getId());
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie3() {
        long id = 1000000000000000000L;
        given(movieRepository.findByIdWithCastsAndDirector(id)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> movieService.getMovie(id));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 영화를 찾을 수 없습니다.".formatted(id));
    }
}