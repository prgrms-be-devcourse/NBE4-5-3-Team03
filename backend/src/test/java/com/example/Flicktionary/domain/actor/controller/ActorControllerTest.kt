package com.example.Flicktionary.domain.actor.controller

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.service.ActorService
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.global.exception.GlobalExceptionHandler
import com.example.Flicktionary.global.exception.ServiceException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ActorControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var actorService: ActorService

    @InjectMocks
    private lateinit var actorController: ActorController

    private lateinit var testActor: Actor
    private lateinit var testMovie: Movie
    private lateinit var testSeries: Series

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(actorController)
            .setControllerAdvice(GlobalExceptionHandler()) // 예외 핸들러 적용
            .build()

        testActor = Actor("actor", "actor.png")
        testActor.id = 1L

        testMovie = Movie("movie", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", "")
        testMovie.id = 1L

        testSeries = Series("series", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", "")
        testSeries.id = 1L
    }
    
    @Test
    @DisplayName("배우 상세 조회 - 성공")
    fun getActor1() {
        // Given
        given(actorService.getActorById(1L)).willReturn(testActor)
        given(actorService.getMoviesByActorId(1L)).willReturn(listOf(testMovie))
        given(actorService.getSeriesByActorId(1L)).willReturn(listOf(testSeries))

        // When & Then
        mockMvc.perform(get(("/api/actors/1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("actor"))
            .andExpect(jsonPath("$.data.profilePath").value("actor.png"))
            .andExpect(jsonPath("$.data.movies[0].title").value("movie"))
            .andExpect(jsonPath("$.data.series[0].title").value("series"))
    }

    @Test
    @DisplayName("배우 상세 조회 - 실패 - 없는 배우 조회")
    fun getActor2() {
        // Given
        val id: Long = 999
        given(actorService.getActorById(id)).willThrow(
            ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 배우를 찾을 수 없습니다.")
        )

        val resultActions = mockMvc.perform(get("/api/actors/$id")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo { print(it) }

        // When & Then
        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value("${id}번 배우를 찾을 수 없습니다."))
    }

    /**
     * ✅ 배우 목록 조회 API 테스트 (성공)
     */
    @Test
    @DisplayName("배우 목록 조회 - 성공")
    fun getActors() {
        // Given
        val page = 1
        val pageSize = 10
        val pageable = PageRequest.of(page - 1, pageSize)
        val actorPage = PageImpl(listOf(testActor), pageable, 1)

        given(actorService.getActors(any<String>(), eq(page), eq(pageSize))).willReturn(actorPage)

        // When & Then
        mockMvc.perform(get("/api/actors")
            .param("keyword", "test")
            .param("page", page.toString())
            .param("pageSize", pageSize.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].id").value(1))
            .andExpect(jsonPath("$.data.items[0].name").value("actor"))
    }
}