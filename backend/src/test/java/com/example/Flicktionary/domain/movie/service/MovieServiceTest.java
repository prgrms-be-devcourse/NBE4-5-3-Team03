package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.movie.dto.MovieRequest;
import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("영화 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private TmdbService tmdbService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = new Movie("testTitle", "",
                LocalDate.of(2022, 1, 1), "Released",
                "movie.png", 100, "", "");
        testMovie.setId(123L);
        testMovie.setAverageRating(1.23);
        testMovie.setRatingCount(12);
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 기본")
    void getMovies1() {
        String keyword = "", sortBy = "id";
        int page = 1, pageSize = 10;
        // @Mock 애노테이션이 붙은 오브젝트의 메소드 호출시, Pageable 타입의 인수를 획득
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        given(movieRepository.findByTitleLike(any(String.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(testMovie),
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
                        List.of(testMovie),
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
                        List.of(testMovie),
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
                        List.of(testMovie),
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
        given(movieRepository.findByIdWithCastsAndDirector(testMovie.getId()))
                .willReturn(testMovie);

        MovieResponseWithDetail result = movieService.getMovie(testMovie.getId());

        assertThat(result).isNotNull();
        assertEquals(testMovie.getId(), result.getId());
        assertEquals(testMovie.getAverageRating(), result.getAverageRating());
        assertEquals(testMovie.getRatingCount(), result.getRatingCount());
        then(movieRepository).should().findByIdWithCastsAndDirector(testMovie.getId());
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie3() {
        long id = 1000000000000000000L;
        given(movieRepository.findByIdWithCastsAndDirector(id)).willReturn(null);

        Throwable thrown = catchThrowable(() -> movieService.getMovie(id));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 영화를 찾을 수 없습니다.".formatted(id));
    }

    @Test
    @DisplayName("영화 생성 - 성공")
    void createMovie1() {
        // given
        MovieRequest request = new MovieRequest(
                "movie",
                "overview",
                LocalDate.of(2022, 1, 1),
                "Released",
                "posterPath",
                100,
                "productionCountry",
                "productionCompany",
                List.of(1L, 2L),
                List.of(new MovieRequest.MovieCastRequest(1L, "characterName")),
                1L
        );

        Genre genre1 = new Genre(1L, "Action");
        Genre genre2 = new Genre(2L, "Drama");
        Actor actor = new Actor(1L, "Test Actor", null);
        Director director = new Director(1L, "Test Director", null);

        Movie savedMovie = new Movie(
                "movie",
                "overview",
                LocalDate.of(2022, 1, 1),
                "Released",
                "posterPath",
                100,
                "productionCountry",
                "productionCompany"
        );
        savedMovie.setId(1L);
        savedMovie.getGenres().addAll(List.of(genre1, genre2));
        savedMovie.getCasts().add(new MovieCast(savedMovie, actor, "characterName"));
        savedMovie.setDirector(director);

        // when
        when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(genre1, genre2));
        when(actorRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        MovieResponseWithDetail response = movieService.createMovie(request);

        // then
        verify(movieRepository).save(any(Movie.class));
        assertNotNull(response);
        assertEquals(savedMovie.getId(), response.getId());
        assertEquals(savedMovie.getTitle(), response.getTitle());
        assertEquals("Action", response.getGenres().get(0).getName());
        assertEquals("Drama", response.getGenres().get(1).getName());
        assertEquals("Test Actor", response.getCasts().get(0).getActor().getName());
        assertEquals("characterName", response.getCasts().get(0).getCharacterName());
        assertEquals("Test Director", response.getDirector().getName());
    }

    @Test
    @DisplayName("영화 수정 - 성공")
    void updateMovie1() {
        // given
        Long movieId = 1L;
        MovieRequest request = new MovieRequest(
                "updated title",
                "updated overview",
                LocalDate.of(2023, 1, 1),
                "Released",
                "updated.png",
                120,
                "USA",
                "Updated Company",
                List.of(1L, 2L),
                List.of(new MovieRequest.MovieCastRequest(1L, "new role")),
                1L
        );

        Genre genre1 = new Genre(1L, "Action");
        Genre genre2 = new Genre(2L, "Drama");
        Actor actor = new Actor(1L, "Actor Name", null);
        Director director = new Director(1L, "Director Name", null);

        Movie movie = new Movie(
                "old title", "old overview", LocalDate.of(2020, 1, 1),
                "old status", "old.png", 90, "Korea", "Old Company"
        );
        movie.setId(movieId);

        // mocking
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(genre1, genre2));
        when(actorRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));

        // when
        MovieResponseWithDetail response = movieService.updateMovie(movieId, request);

        // then
        assertEquals("updated title", response.getTitle());
        assertEquals("updated overview", response.getOverview());
        assertEquals("updated.png", response.getPosterPath());
        assertEquals("Released", response.getStatus());
        assertEquals(120, response.getRuntime());
        assertEquals("USA", response.getProductionCountry());
        assertEquals("Updated Company", response.getProductionCompany());
        assertEquals(2, response.getGenres().size());
        assertEquals("Action", response.getGenres().get(0).getName());
        assertEquals("Drama", response.getGenres().get(1).getName());
        assertEquals("Actor Name", response.getCasts().get(0).getActor().getName());
        assertEquals("new role", response.getCasts().get(0).getCharacterName());
        assertEquals("Director Name", response.getDirector().getName());
    }

    @Test
    @DisplayName("영화 수정 - 실패 - 없는 영화")
    void updateMovie2() {
        // given
        Long movieId = 1L;
        MovieRequest request = new MovieRequest(
                "updated title",
                "updated overview",
                LocalDate.of(2023, 1, 1),
                "Released",
                "updated.png",
                120,
                "USA",
                "Updated Company",
                List.of(1L, 2L),
                List.of(new MovieRequest.MovieCastRequest(1L, "new role")),
                1L
        );

        // mocking
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> movieService.updateMovie(movieId, request));

        // then
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 영화를 찾을 수 없습니다.".formatted(movieId));
    }

    @Test
    @DisplayName("영화 삭제 - 성공")
    void deleteMovie1() {
        // given
        Long movieId = 1L;
        Movie movie = new Movie();
        movie.setId(movieId);

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        // when
        movieService.deleteMovie(movieId);

        // then
        verify(movieRepository).delete(movie);
    }

    @Test
    @DisplayName("영화 삭제 - 실패 - 없는 영화")
    void deleteMovie2() {
        // given
        Long movieId = 1L;
        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> movieService.deleteMovie(movieId));

        // then
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 영화를 찾을 수 없습니다.".formatted(movieId));
    }

}