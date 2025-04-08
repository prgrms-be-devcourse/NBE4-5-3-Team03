package com.example.Flicktionary.domain.movie.controller;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.director.dto.DirectorDto;
import com.example.Flicktionary.domain.genre.dto.GenreDto;
import com.example.Flicktionary.domain.movie.dto.MovieCastDto;
import com.example.Flicktionary.domain.movie.dto.MovieRequest;
import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.service.MovieService;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("영화 도메인 컨트롤러 테스트")
@Import({MovieService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Movie testMovie1;

    private Movie testMovie2;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        testMovie1 = new Movie("testTitle1", "",
                LocalDate.of(2022, 1, 1), "",
                "movie.png", 100, "", "");
        testMovie1.setId(123L);
        testMovie1.setAverageRating(1.23);
        testMovie1.setRatingCount(123);

        testMovie2 = new Movie("testTitle2", "",
                LocalDate.of(2022, 1, 1), "",
                "movie.png", 100, "", "");
        testMovie2.setId(456L);
        testMovie2.setAverageRating(4.56);
        testMovie2.setRatingCount(456);
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공")
    void getMovies1() throws Exception {
        String keyword = "", sortBy = "id";
        int page = 1, pageSize = 10;
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        given(movieService.getMovies(
                stringCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture(),
                stringCaptor.capture())
        ).willReturn(new PageDto<>(new PageImpl<>(
                List.of(new MovieResponse(testMovie1), new MovieResponse(testMovie2)),
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                10)));

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .param("keyword", keyword)
                        .param("page", "%d".formatted(page))
                        .param("pageSize", "%d".formatted(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(testMovie1.getId()))
                .andExpect(jsonPath("$.data.items[1].id").value(testMovie2.getId()))
                .andExpect(jsonPath("$.data.totalPages").value(10 / pageSize))
                .andExpect(jsonPath("$.data.totalItems").value(10));
        List<String> stringArgs = stringCaptor.getAllValues();
        List<Integer> integerArgs = integerCaptor.getAllValues();

        assertEquals(keyword, stringArgs.getFirst());
        assertEquals(page, integerArgs.getFirst());
        assertEquals(pageSize, integerArgs.get(1));
        assertEquals(sortBy, stringArgs.get(1));
        then(movieService).should().getMovies(keyword, page, pageSize, sortBy);
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    void getMovies2() throws Exception {
        given(movieService.getMovies(any(String.class), any(Integer.class), any(Integer.class), any(String.class)))
                .willThrow(new ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬기준입니다."));

        ResultActions resultActions = mvc.perform(get("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovies"))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("잘못된 정렬기준입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공")
    void getMovie1() throws Exception {
        given(movieService.getMovie(testMovie1.getId()))
                .willReturn(new MovieResponseWithDetail(testMovie1));

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(testMovie1.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"))
                .andExpect(jsonPath("$.data.id").value(testMovie1.getId()))
                .andExpect(jsonPath("$.data.title").value(testMovie1.getTitle()))
                // TODO: 배우와 장르 정보가 있는 영화 엔티티를 작성해 테스트를 실행할 것
                .andExpect(jsonPath("$.data.casts").isEmpty())
                .andExpect(jsonPath("$.data.genres").isEmpty());
        then(movieService).should().getMovie(testMovie1.getId());
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    void getMovie2() throws Exception {
        long id = 1000000000000000000L;
        given(movieService.getMovie(id)).willThrow(
                new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 영화를 찾을 수 없습니다.".formatted(id))
        );

        ResultActions resultActions = mvc.perform(get("/api/movies/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("getMovie"))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("%d번 영화를 찾을 수 없습니다.".formatted(id)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("영화 생성 - 성공")
    void createMovie1() throws Exception {
        // given
        MovieRequest request = new MovieRequest(
                "movie",
                "overview",
                LocalDate.of(2022, 1, 1),
                "Released",
                "movie.png",
                100,
                "Korea",
                "Test Company",
                List.of(1L, 2L),
                List.of(new MovieRequest.MovieCastRequest(1L, "characterName")),
                1L
        );

        MovieResponseWithDetail response = new MovieResponseWithDetail(
                1L,
                "movie",
                "overview",
                LocalDate.of(2022, 1, 1),
                "movie.png",
                "Released",
                100,
                "Korea",
                "Test Company",
                0.0,
                0,
                List.of(
                        new GenreDto(1L, "Action"),
                        new GenreDto(2L, "Drama")
                ),
                List.of(
                        new MovieCastDto(
                                new ActorDto(1L, "Test Actor", null),
                                "characterName"
                        )
                ),
                new DirectorDto(1L, "Test Director", null)
        );

        // when
        when(movieService.createMovie(Mockito.any(MovieRequest.class)))
                .thenReturn(response);

        // then
        ResultActions resultActions = mvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("movie"))
                .andExpect(jsonPath("$.data.genres[0].name").value("Action"))
                .andExpect(jsonPath("$.data.casts[0].actor.name").value("Test Actor"));
    }

    @Test
    @DisplayName("영화 수정 - 성공")
    void updateMovie1() throws Exception {
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

        MovieResponseWithDetail response = new MovieResponseWithDetail(
                1L,
                "Updated Movie",
                "Updated Overview",
                LocalDate.of(2023, 1, 1),
                "updated.png",
                "Released",
                120,
                "USA",
                "Updated Company",
                0.0,
                0,
                List.of(
                        new GenreDto(1L, "Action"),
                        new GenreDto(2L, "Drama")
                ),
                List.of(
                        new MovieCastDto(
                                new ActorDto(1L, "Actor Name", null),
                                "new roll"
                        )
                ),
                new DirectorDto(1L, "Director Name", null)
        );

        when(movieService.updateMovie(movieId, request))
                .thenReturn(response);

        ResultActions resultActions = mvc.perform(put("/api/movies/%d".formatted(movieId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("정상 처리되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Updated Movie"))
                .andExpect(jsonPath("$.data.genres[0].name").value("Action"))
                .andExpect(jsonPath("$.data.casts[0].actor.name").value("Actor Name"))
                .andExpect(jsonPath("$.data.casts[0].characterName").value("new roll"));
    }

    @Test
    @DisplayName("영화 수정 - 실패")
    void updateMovie2() throws Exception {
        Long movieId = 1000000000L;
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

        given(movieService.updateMovie(movieId, request)).willThrow(
                new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 영화를 찾을 수 없습니다.".formatted(movieId))
        );

        ResultActions resultActions = mvc.perform(put("/api/movies/%d".formatted(movieId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(MovieController.class))
                .andExpect(handler().methodName("updateMovie"))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("%d번 영화를 찾을 수 없습니다.".formatted(movieId)))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    @DisplayName("영화 삭제 - 성공")
    void deleteMovie1() throws Exception {
        // given
        Long movieId = 1L;

        // when
        doNothing().when(movieService).deleteMovie(movieId);

        // then
        mvc.perform(delete("/api/movies/{id}", movieId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code").value("204"))
                .andExpect(jsonPath("$.message").value("No Content"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("영화 삭제 - 실패 - 없는 영화")
    void deleteMovie_NotFound() throws Exception {
        // given
        Long movieId = 999L;

        // when
        doThrow(new ServiceException(404, "%d번 영화를 찾을 수 없습니다.".formatted(movieId)))
                .when(movieService).deleteMovie(movieId);

        // then
        mvc.perform(delete("/api/movies/%d".formatted(movieId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("%d번 영화를 찾을 수 없습니다.".formatted(movieId)));
    }
}