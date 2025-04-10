package com.example.Flicktionary.domain.director.controller

import com.example.Flicktionary.domain.director.dto.DirectorRequest
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.service.DirectorService
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.global.exception.GlobalExceptionHandler
import com.example.Flicktionary.global.exception.ServiceException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class DirectorControllerTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var directorService: DirectorService

    @InjectMocks
    private lateinit var directorController: DirectorController

    private val objectMapper = ObjectMapper()

    private lateinit var director: Director
    private lateinit var movie: Movie
    private lateinit var series: Series

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(directorController)
            .setControllerAdvice(GlobalExceptionHandler()) // 예외 핸들러 적용
            .build()

        director = Director("director", "director.png").apply { id = 1L }

        movie = Movie(
            "movie", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", ""
        ).apply {
            id = 1L
            this.director = this@DirectorControllerTest.director
        }

        series = Series(
            "series", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        ).apply {
            id = 1L
            this.director = this@DirectorControllerTest.director
        }
    }


    @DisplayName("감독 목록 조회 - 성공")
    @Test
    fun getDirectors1() {
        // Given
        val page = 1
        val pageSize = 10
        val pageable: Pageable = PageRequest.of(page - 1, pageSize)
        val directorPage = PageImpl(listOf(director), pageable, 1)

        given(
            directorService.getDirectors(
                any(), eq(page), eq(pageSize)
            )
        ).willReturn(directorPage)

        // When & Then
        mockMvc.perform(
            get("/api/directors")
                .param("keyword", "")
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].id").value(1))
            .andExpect(jsonPath("$.data.items[0].name").value("director"))
    }

    @DisplayName("감독 상세 조회 - 성공")
    @Test
    fun getDirector1() {
        // Given
        given(directorService.getDirector(1L)).willReturn(director)
        given(directorService.getMoviesByDirectorId(1L))
            .willReturn(listOf(movie))
        given(
            directorService.getSeriesByDirectorId(1L)
        ).willReturn(listOf(series))

        // When & Then
        mockMvc.perform(get("/api/directors/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("director"))
            .andExpect(jsonPath("$.data.profilePath").value("director.png"))
            .andExpect(jsonPath("$.data.movies[0].title").value("movie"))
            .andExpect(jsonPath("$.data.series[0].title").value("series"))
    }

    @DisplayName("감독 상세 조회 - 실패 - 없는 감독 조회")
    @Test
    fun director2() {
        val id: Long = 999
        given(directorService.getDirector(id)).willThrow(
            ServiceException(
                HttpStatus.NOT_FOUND.value(),
                "${id}번 감독을 찾을 수 없습니다."
            )
        )

        val resultActions = mockMvc.perform(
            get("/api/directors/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value("${id}번 감독을 찾을 수 없습니다."))
    }

    @DisplayName("감독 등록 - 성공")
    @Test
    fun createDirector1() {
        val request = DirectorRequest("director", "director.png")

        given(directorService.createDirector(request)).willReturn(director)

        val resultActions = mockMvc.perform(
            post("/api/directors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())

        resultActions
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("director"))
            .andExpect(jsonPath("$.data.profilePath").value("director.png"))
    }

    @DisplayName("감독 등록 - 실패 - 이미 존재하는 감독")
    @Test
    fun createDirector2() {
        val request = DirectorRequest("director", "director.png")

        given(directorService.createDirector(request)).willThrow(
            ServiceException(
                HttpStatus.CONFLICT.value(),
                "이미 존재하는 감독입니다."
            )
        )

        val resultActions = mockMvc.perform(
            post("/api/directors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())

        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("$.message").value("이미 존재하는 감독입니다."))
    }

    @Test
    @DisplayName("감독 수정 - 성공")
    fun updateDirector1() {
        val id = 1L
        val request = DirectorRequest("director", "director.png")

        given(directorService.updateDirector(id, request)).willReturn(director)

        mockMvc.perform(
            put("/api/directors/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("director"))
            .andExpect(jsonPath("$.data.profilePath").value("director.png"))
    }

    @Test
    @DisplayName("감독 수정 - 실패 - 없는 감독 수정")
    fun updateDirector2() {
        val id = 999L
        val request = DirectorRequest("director", "director.png")

        given(directorService.updateDirector(id, request)).willThrow(
            ServiceException(
                HttpStatus.NOT_FOUND.value(),
                "${id}번 감독을 찾을 수 없습니다."
            )
        )

        mockMvc.perform(
            put("/api/directors/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("${id}번 감독을 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("감독 삭제 - 성공")
    fun deleteDirector1() {
        val id = 999L

        doNothing().whenever(directorService).deleteDirector(id)

        mockMvc.perform(
            delete("/api/directors/${id}")
        )
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.code").value("204"))
            .andExpect(jsonPath("$.message").value("No Content"))
            .andExpect(jsonPath("$.data").doesNotExist())
    }

    @Test
    @DisplayName("감독 삭제 - 실패 - 없는 감독 삭제")
    fun deleteDirector2() {
        val id = 999L

        given(directorService.deleteDirector(id)).willThrow(
            ServiceException(
                HttpStatus.NOT_FOUND.value(),
                "${id}번 감독을 찾을 수 없습니다."
            )
        )

        mockMvc.perform(
            delete("/api/directors/${id}")
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("${id}번 감독을 찾을 수 없습니다."))
    }
}