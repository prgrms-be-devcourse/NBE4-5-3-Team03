package com.example.Flicktionary.domain.director.controller;

import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.service.DirectorService;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.global.exception.GlobalExceptionHandler;
import com.example.Flicktionary.global.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DirectorControllerTest {
    private MockMvc mockMvc;

    @Mock
    private DirectorService directorService;

    @InjectMocks
    private DirectorController directorController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Director director;
    private Movie movie;
    private Series series;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(directorController)
                .setControllerAdvice(new GlobalExceptionHandler()) // 예외 핸들러 적용
                .build();

        director = new Director(1L, "director", "director.png");

        movie = new Movie(1L, "movie", "",
                LocalDate.of(2022, 1, 1), "",
                "movie.png", 100, "", "");
        movie.setId(1L);
        movie.setDirector(director);

        series = Series.builder()
                .id(1L)
                .tmdbId(1L)
                .title("series")
                .posterPath("series.png")
                .releaseStartDate(LocalDate.of(2022, 1, 1))
                .releaseEndDate(LocalDate.of(2023, 1, 1))
                .director(director)
                .build();
    }

    @Test
    @DisplayName("감독 목록 조회 - 성공")
    void getDirectors1() throws Exception {
        // Given
        int page = 1, pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Director> directorPage = new PageImpl<>(List.of(director), pageable, 1);

        given(directorService.getDirectors(any(String.class), eq(page), eq(pageSize))).willReturn(directorPage);

        // When & Then
        mockMvc.perform(get("/api/directors")
                        .param("keyword", "")
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("director"));
    }

    @Test
    @DisplayName("감독 상세 조회 - 성공")
    void getDirector1() throws Exception {
        // Given
        given(directorService.getDirector(1L)).willReturn(director);
        given(directorService.getMoviesByDirectorId(1L)).willReturn(List.of(movie));
        given(directorService.getSeriesByDirectorId(1L)).willReturn(List.of(series));

        // When & Then
        mockMvc.perform(get("/api/directors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("director"))
                .andExpect(jsonPath("$.data.profilePath").value("director.png"))
                .andExpect(jsonPath("$.data.movies[0].title").value("movie"))
                .andExpect(jsonPath("$.data.series[0].title").value("series"));
    }

    @Test
    @DisplayName("감독 상세 조회 - 실패 - 없는 감독 조회")
    void getDirector2() throws Exception {
        long id = 999;
        given(directorService.getDirector(id)).willThrow(
                new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 감독을 찾을 수 없습니다.".formatted(id))
        );

        ResultActions resultActions = mockMvc.perform(get("/api/directors/%d".formatted(id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("%d번 감독을 찾을 수 없습니다.".formatted(id)));
    }
}