package com.example.Flicktionary.domain.movie.controller

import com.example.Flicktionary.domain.actor.dto.ActorDto
import com.example.Flicktionary.domain.director.dto.DirectorDto
import com.example.Flicktionary.domain.genre.dto.GenreDto
import com.example.Flicktionary.domain.movie.dto.MovieCastDto
import com.example.Flicktionary.domain.movie.dto.MovieRequest
import com.example.Flicktionary.domain.movie.dto.MovieRequest.MovieCastRequest
import com.example.Flicktionary.domain.movie.dto.MovieResponse
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.service.MovieService
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import com.example.Flicktionary.global.security.CustomUserDetailsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate

@DisplayName("영화 도메인 컨트롤러 테스트")
@Import(
    MovieService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(MovieController::class)
@AutoConfigureMockMvc(addFilters = false)
internal class MovieControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var movieService: MovieService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    private val objectMapper = ObjectMapper()

    private lateinit var testMovie1: Movie

    private lateinit var testMovie2: Movie

    @BeforeEach
    fun setUp() {
        objectMapper.registerModule(JavaTimeModule())

        testMovie1 = Movie(
            "testTitle1", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", ""
        )
        testMovie1.id = 123L
        testMovie1.averageRating = 1.23
        testMovie1.ratingCount = 123

        testMovie2 = Movie(
            "testTitle2", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", ""
        )
        testMovie2.id = 456L
        testMovie2.averageRating = 4.56
        testMovie2.ratingCount = 456
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공")
    @Throws(Exception::class)
    fun testGetMovies1() {
        val keyword = ""
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        // Stub에 ArgumentCaptor 직접 넣지 않고 값만 전달
        BDDMockito.given(
            movieService.getMovies(
                keyword,
                page,
                pageSize,
                sortBy
            )
        ).willReturn(
            PageDto(
                PageImpl(
                    listOf(MovieResponse(testMovie1), MovieResponse(testMovie2)),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, sortBy)),
                    2
                )
            )
        )

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/api/movies")
                .param("keyword", keyword)
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("sortBy", sortBy)
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.handler().handlerType(MovieController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getMovies"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[0].id").value(testMovie1.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[1].id").value(testMovie2.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalItems").value(2))
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    @Throws(Exception::class)
    fun testgetMovies2() {
        val keyword = ""
        val sortBy = "invalid"
        val page = 1
        val pageSize = 10

        BDDMockito.given(
            movieService.getMovies(
                keyword, page, pageSize, sortBy
            )
        )
            .willThrow(ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬기준입니다."))

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/api/movies")
                .param("keyword", keyword)
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("sortBy", sortBy)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.handler().handlerType(MovieController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getMovies"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 정렬기준입니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공")
    @Throws(Exception::class)
    fun testgetMovie1() {
        BDDMockito.given(movieService.getMovie(testMovie1.id))
            .willReturn(MovieResponseWithDetail(testMovie1))

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/api/movies/${testMovie1.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(MovieController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getMovie"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(testMovie1.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.title").value(testMovie1.title)
            ) // TODO: 배우와 장르 정보가 있는 영화 엔티티를 작성해 테스트를 실행할 것
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts").isEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.genres").isEmpty())
        BDDMockito.then(movieService).should().getMovie(testMovie1.id)
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    @Throws(Exception::class)
    fun testgetMovie2() {
        val id = 1000000000000000000L
        BDDMockito.given(movieService.getMovie(id)).willThrow(
            ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 영화를 찾을 수 없습니다.")
        )

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/api/movies/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.handler().handlerType(MovieController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getMovie"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${id}번 영화를 찾을 수 없습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("영화 생성 - 성공")
    @Throws(Exception::class)
    fun createMovie1() {
        // given
        val request = MovieRequest(
            "movie",
            "overview",
            LocalDate.of(2022, 1, 1),
            "Released",
            "movie.png",
            100,
            "Korea",
            "Test Company",
            listOf(1L, 2L),
            listOf(MovieCastRequest(1L, "characterName")),
            1L
        )

        val response = MovieResponseWithDetail(
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
            listOf(
                GenreDto(1L, "Action"),
                GenreDto(2L, "Drama")
            ),
            listOf(
                MovieCastDto(
                    ActorDto(1L, "Test Actor", null),
                    "characterName"
                )
            ),
            DirectorDto(1L, "Test Director", null)
        )

        // when
        whenever(movieService.createMovie(request))
            .thenReturn(response)

        // then
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Created"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("movie"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.genres[0].name").value("Action"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts[0].actor.name").value("Test Actor"))
    }

    @Test
    @DisplayName("영화 수정 - 성공")
    @Throws(Exception::class)
    fun updateMovie1() {
        // given
        val movieId = 1L
        val request = MovieRequest(
            "updated title",
            "updated overview",
            LocalDate.of(2023, 1, 1),
            "Released",
            "updated.png",
            120,
            "USA",
            "Updated Company",
            listOf(1L, 2L),
            listOf(MovieCastRequest(1L, "new role")),
            1L
        )

        val response = MovieResponseWithDetail(
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
            listOf(
                GenreDto(1L, "Action"),
                GenreDto(2L, "Drama")
            ),
            listOf(
                MovieCastDto(
                    ActorDto(1L, "Actor Name", null),
                    "new roll"
                )
            ),
            DirectorDto(1L, "Director Name", null)
        )

        whenever(movieService.updateMovie(movieId, request))
            .thenReturn(response)

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/api/movies/${movieId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("정상 처리되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Updated Movie"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.genres[0].name").value("Action"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts[0].actor.name").value("Actor Name"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts[0].characterName").value("new roll"))
    }

    @Test
    @DisplayName("영화 수정 - 실패")
    @Throws(Exception::class)
    fun updateMovie2() {
        val movieId = 1000000000L
        val request = MovieRequest(
            "updated title",
            "updated overview",
            LocalDate.of(2023, 1, 1),
            "Released",
            "updated.png",
            120,
            "USA",
            "Updated Company",
            listOf(1L, 2L),
            listOf(MovieCastRequest(1L, "new role")),
            1L
        )

        BDDMockito.given(movieService.updateMovie(movieId, request)).willThrow(
            ServiceException(HttpStatus.NOT_FOUND.value(), "${movieId}번 영화를 찾을 수 없습니다.")
        )

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/api/movies/${movieId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.handler().handlerType(MovieController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("updateMovie"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${movieId}번 영화를 찾을 수 없습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("영화 삭제 - 성공")
    @Throws(Exception::class)
    fun deleteMovie1() {
        // given
        val movieId = 1L

        // when
        Mockito.doNothing().whenever(movieService).deleteMovie(movieId)

        // then
        mvc.perform(MockMvcRequestBuilders.delete("/api/movies/${movieId}"))
            .andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("204"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No Content"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
    }

    @Test
    @DisplayName("영화 삭제 - 실패 - 없는 영화")
    @Throws(Exception::class)
    fun deleteMovie_NotFound() {
        // given
        val movieId = 999L

        // when
        Mockito.doThrow(ServiceException(404, "${movieId}번 영화를 찾을 수 없습니다."))
            .whenever(movieService).deleteMovie(movieId)

        // then
        mvc.perform(MockMvcRequestBuilders.delete("/api/movies/${movieId}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${movieId}번 영화를 찾을 수 없습니다."))
    }
}