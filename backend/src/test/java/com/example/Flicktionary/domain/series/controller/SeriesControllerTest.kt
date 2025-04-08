package com.example.Flicktionary.domain.series.controller

import com.example.Flicktionary.domain.actor.dto.ActorDto
import com.example.Flicktionary.domain.director.dto.DirectorDto
import com.example.Flicktionary.domain.genre.dto.GenreDto
import com.example.Flicktionary.domain.series.dto.SeriesCastDto
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse
import com.example.Flicktionary.domain.series.dto.SeriesRequest
import com.example.Flicktionary.domain.series.dto.SeriesRequest.SeriesCastRequest
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.service.SeriesService
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
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate

@DisplayName("시리즈 도메인 컨트롤러 테스트")
@Import(
    SeriesService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(SeriesController::class)
@AutoConfigureMockMvc(addFilters = false)
class SeriesControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var seriesService: SeriesService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    private val objectMapper = ObjectMapper()

    private lateinit var series1: Series
    private lateinit var series2: Series
    private lateinit var series3: Series

    @BeforeEach
    fun setUp() {
        objectMapper.registerModule(JavaTimeModule())

        series1 = Series(
            "Series 1", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        )
        series1.id = 1L
        series1.averageRating = 2.1
        series1.ratingCount = 150

        series2 = Series(
            "Series 2", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        )
        series2.id = 2L
        series2.averageRating = 3.6
        series2.ratingCount = 100

        series3 = Series(
            "Series 3", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        )
        series3.id = 3L
        series3.averageRating = 3.0
        series3.ratingCount = 50
    }

    @Test
    @DisplayName("Series 목록 조회")
    @Throws(Exception::class)
    fun testGetSeries() {
        // 테스트할 파라미터 설정
        val keyword = ""
        val page = 1
        val pageSize = 2
        val sortBy = "id"

        //given
        val mockSeriesList = listOf(
            series1, series2, series3
        )
        val mockSeriesPage: Page<Series> =
            PageImpl(mockSeriesList, PageRequest.of(page - 1, pageSize), mockSeriesList.size.toLong())
        val result = PageDto(mockSeriesPage.map { series: Series ->
            SeriesSummaryResponse(
                series
            )
        })

        // mockSeriesPage를 seriesService.getSeries()에서 반환하도록 설정(when)
        Mockito.`when`(
            seriesService.getSeries(keyword, page, pageSize, sortBy)
        ).thenReturn(mockSeriesPage)
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/api/series")
                .param("keyword", keyword)
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("sortBy", sortBy)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        // 예상 반환값과 API 요청 반환 값 비교(then)
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
            .andExpect(
                MockMvcResultMatchers.handler().handlerType(SeriesController::class.java)
            ) // 호출된 핸들러가 SeriesController인지 확인
            .andExpect(MockMvcResultMatchers.handler().methodName("getSeries")) // 호출된 메서드가 getSeries인지 확인
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items").isArray()) // 응답의 items가 배열인지 확인
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.items[0].id").value(result.items[0].id)
            ) // 첫 번째 아이템의 ID 검증
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.items[1].id").value(result.items[1].id)
            ) // 두 번째 아이템의 ID 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(result.totalPages)) // 전체 페이지 수 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalItems").value(result.totalItems)) // 전체 아이템 수 검증
    }

    @Test
    @DisplayName("Series 상세 조회")
    @Throws(Exception::class)
    fun testGetSeriesDetail() {
        // given
        val seriesId = 1L
        val response = SeriesDetailResponse(
            seriesId, "Test Series", "http://test.com/image.jpg",
            4.5, 100, 10, "Test Plot", "Test Company", "Test Nation",
            LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1),
            "Completed", emptyList(), emptyList(), null
        )

        //when
        Mockito.`when`(seriesService.getSeriesDetail(seriesId)).thenReturn(response)

        //then
        mvc.perform(
            MockMvcRequestBuilders.get("/api/series/{id}", seriesId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(seriesId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Test Series"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.averageRating").value(4.5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.ratingCount").value(100))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.episode").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.plot").value("Test Plot"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.company").value("Test Company"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nation").value("Test Nation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("Completed"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("시리즈 생성  - 성공")
    @Throws(Exception::class)
    fun createMovie1() {
        // given
        val request = SeriesRequest(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath", 10,
            "productionCountry",
            "productionCompany",
            listOf(1L, 2L),
            listOf(SeriesCastRequest(1L, "characterName")),
            1L
        )

        val response = SeriesDetailResponse(
            1L,
            "title",
            "posterPath",
            0.0,
            0,
            10,
            "overview",
            "productionCountry",
            "productionCompany",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            listOf(
                GenreDto(1L, "Action"),
                GenreDto(2L, "Drama")
            ),
            listOf(
                SeriesCastDto(
                    ActorDto(1L, "name", null),
                    "characterName"
                )
            ),
            DirectorDto(1L, "name", null)
        )

        //when
        Mockito.`when`(seriesService.createSeries(request)).thenReturn(response)

        //then
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/api/series")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Created"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("title"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.genres[0].name").value("Action"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts[0].actor.name").value("name"))
    }

    @Test
    @DisplayName("영화 수정 - 성공")
    @Throws(Exception::class)
    fun updateMovie1() {
        // given
        val id = 1L
        val request = SeriesRequest(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath", 10,
            "productionCountry",
            "productionCompany",
            listOf(1L, 2L),
            listOf(SeriesCastRequest(1L, "characterName")),
            1L
        )

        val response = SeriesDetailResponse(
            1L,
            "title",
            "posterPath",
            0.0,
            0,
            10,
            "overview",
            "productionCountry",
            "productionCompany",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            listOf(
                GenreDto(1L, "Action"),
                GenreDto(2L, "Drama")
            ),
            listOf(
                SeriesCastDto(
                    ActorDto(1L, "name", null),
                    "characterName"
                )
            ),
            DirectorDto(1L, "name", null)
        )

        //when
        Mockito.`when`(seriesService.updateSeries(id, request)).thenReturn(response)

        //then
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/api/series/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("정상 처리되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("title"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.genres[0].name").value("Action"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.casts[0].actor.name").value("name"))
    }

    @Test
    @DisplayName("시리즈 수정 - 실패 - 없는 시리즈")
    @Throws(Exception::class)
    fun updateMovie2() {
        // given
        val id = 1000000000L
        val request = SeriesRequest(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath", 10,
            "productionCountry",
            "productionCompany",
            listOf(1L, 2L),
            listOf(SeriesCastRequest(1L, "characterName")),
            1L
        )

        // when
        Mockito.`when`(seriesService.updateSeries(id, request)).thenThrow(
            ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 시리즈를 찾을 수 없습니다.")
        )

        // then
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/api/series/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.handler().handlerType(SeriesController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("updateSeries"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${id}번 시리즈를 찾을 수 없습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("시리즈 삭제 - 성공")
    @Throws(Exception::class)
    fun deleteSeries1() {
        // given
        val id = 1L

        // when
        Mockito.doNothing().`when`(seriesService).deleteSeries(id)

        // then
        mvc.perform(MockMvcRequestBuilders.delete("/api/series/{id}", id))
            .andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("204"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No Content"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist())
    }

    @Test
    @DisplayName("시리즈 삭제 - 실패 - 없는 시리즈")
    @Throws(Exception::class)
    fun deleteSeries2() {
        // given
        val id = 999L

        // when
        Mockito.doThrow(ServiceException(404, "${id}번 시리즈를 찾을 수 없습니다."))
            .`when`(seriesService).deleteSeries(id)

        // then
        mvc.perform(MockMvcRequestBuilders.delete("/api/series/${id}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${id}번 시리즈를 찾을 수 없습니다."))
    }
}
