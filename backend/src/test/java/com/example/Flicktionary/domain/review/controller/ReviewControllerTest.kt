package com.example.Flicktionary.domain.review.controller

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.review.dto.ReviewDto
import com.example.Flicktionary.domain.review.service.ReviewService
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.security.CustomUserDetailsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@DisplayName("리뷰 도메인 컨트롤러 테스트")
@Import(
    ReviewService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(ReviewController::class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var reviewService: ReviewService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    // 변수 설정
    private val testUser = UserAccount(
        10000L, "테스트용 유저", "test12345", "test@email.com", "테스트 유저", UserAccountType.USER
    )

    private lateinit var testMovie: Movie

    private lateinit var testSeries: Series

    private lateinit var reviewDto1: ReviewDto

    private lateinit var reviewDto2: ReviewDto

    @BeforeEach
    fun setUp() {
        testMovie = Movie(
            tmdbId = 10000000000L,
            title = "테스트용 영화 제목",
            overview = "테스트용 영화 줄거리",
            releaseDate = LocalDate.of(2024, 1, 1),
            status = "상영 중",
            posterPath = "테스트용 이미지",
            runtime = 100,
            productionCountry = "KR",
            productionCompany = "테스트용 제작사"
        ).apply {
            id = 123L
            averageRating = 4.0
        }

        testSeries = Series(
            tmdbId = 10000000000L,
            title = "테스트용 드라마 제목",
            overview = "테스트용 드라마 줄거리",
            releaseStartDate = LocalDate.of(2024, 1, 1),
            releaseEndDate = LocalDate.of(2200, 1, 2),
            status = "상영 중",
            posterPath = "테스트용 이미지",
            episodeNumber = 12,
            productionCountry = "KR",
            productionCompany = "테스트용 제작사"
        ).apply {
            id = 321L
            averageRating = 4.5
            ratingCount = 10
        }

        reviewDto1 = ReviewDto(
            id = 123L,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = testMovie.id,
            seriesId = null, // 영화 리뷰이므로 seriesId는 null
            rating = 5,
            content = "테스트용 리뷰 내용 (영화)"
        )

        reviewDto2 = ReviewDto(
            id = 321L,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = null, // 드라마 리뷰이므로 movieId는 null
            seriesId = testSeries.id,
            rating = 5,
            content = "테스트용 리뷰 내용 (드라마)"
        )
    }

    @Test
    @DisplayName("리뷰 생성")
    fun createReview() {
        given(reviewService.createReview(reviewDto1)).willReturn(reviewDto1)

        // mockMvc로 post 요청 후 Content-Type 설정과 요청 본문 설정
        mockMvc.perform(
            post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto1))
        )
            .andExpect(status().isCreated()) // 응답 상태 검증
            .andExpect(
                jsonPath("$.data.content") // JSON 응답 검증
                    .value("테스트용 리뷰 내용 (영화)")
            )

        verify(reviewService).createReview(reviewDto1)
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    fun getAllReviews() {
        // 변수 설정
        val page = 0
        val size = 10
        val reviewList = listOf(reviewDto1, reviewDto2)
        val totalItems = reviewList.size
        val totalPages = 1 // size가 10이고 아이템이 2개이므로 1페이지
        val curPageNo = page + 1
        val sortBy = "id: ASC" // 예시로 정렬 기준 설정

        val reviewsPageDto = PageDto(
            items = reviewList,
            totalPages = totalPages,
            totalItems = totalItems,
            curPageNo = curPageNo,
            pageSize = size,
            sortBy = sortBy
        )

        given(
            reviewService.findAllReviews(
                page,
                size
            )
        ).willReturn(reviewsPageDto)

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(
            get("/api/reviews")
                .param("page", page.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.items[0].content")
                    .value("테스트용 리뷰 내용 (영화)")
            )
            .andExpect(
                jsonPath("$.data.items[1].content")
                    .value("테스트용 리뷰 내용 (드라마)")
            )

        then(reviewService).should().findAllReviews(page, size)
    }

    @Test
    @DisplayName("리뷰 수정")
    fun updateReview() {
        // 수정할 리뷰 데이터 생성
        val modifyReview = ReviewDto(
            id = reviewDto1.id,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = testMovie.id,
            seriesId = null,
            rating = 4,
            content = "수정된 테스트용 리뷰"
        )
        given(reviewService.updateReview(reviewDto1.id!!, modifyReview))
            .willReturn(modifyReview)

        // mockMvc로 put 요청 후 검증
        mockMvc.perform(
            put("/api/reviews/${reviewDto1.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyReview))
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.content")
                    .value("수정된 테스트용 리뷰")
            )
            .andExpect(jsonPath("$.data.rating").value(4))

        // 호출되었는지 검증
        verify(reviewService).updateReview(reviewDto1.id!!, modifyReview)
    }

    @Test
    @DisplayName("리뷰 삭제")
    fun deleteReview() {
        val captor = ArgumentCaptor.forClass(Long::class.java)
        doNothing().`when`(reviewService).deleteReview(captor.capture())

        // mockMvc로 delete 요청 후 검증
        mockMvc.perform(delete("/api/reviews/" + reviewDto1.id))
            .andExpect(status().isNoContent())
        val captured = captor.value

        Assertions.assertEquals(reviewDto1.id, captured)
        then(reviewService).should().deleteReview(reviewDto1.id!!)
    }

    @Test
    @DisplayName("특정 영화 리뷰 페이지 조회")
    fun getReviewsMovies() {
        val movieId = requireNotNull(reviewDto1.movieId)

        given(reviewService.reviewMovieDtoPage(movieId, 0, 5))
            .willReturn(
                PageDto(
                    items = listOf(reviewDto1),
                    totalPages = 1,
                    totalItems = 10,
                    curPageNo = 1,
                    pageSize = 5,
                    sortBy = "id: DESC"
                )
            )

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(
            get("/api/reviews/movies/${reviewDto1.movieId}")
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.items[0].content")
                    .value("테스트용 리뷰 내용 (영화)")
            )

        // 호출되었는지 검증
        verify(reviewService).reviewMovieDtoPage(movieId, 0, 5)
    }

    @Test
    @DisplayName("특정 드라마 리뷰 페이지 조회")
    fun getReviewMovieSeries() {
        val longCaptor = ArgumentCaptor.forClass(Long::class.java)
        val integerCaptor = ArgumentCaptor.forClass(Int::class.java)
        given(
            reviewService.reviewSeriesDtoPage(
                longCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture()
            )
        )
            .willReturn(
                PageDto(
                    items = listOf(reviewDto2),
                    totalPages = 1,
                    totalItems = 10,
                    curPageNo = 1,
                    pageSize = 5,
                    sortBy = "id: DESC"
                )
            )

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(
            get("/api/reviews/series/" + reviewDto2.seriesId)
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.items[0].content")
                    .value("테스트용 리뷰 내용 (드라마)")
            )
        val longValue = longCaptor.value
        val integerValues = integerCaptor.allValues

        Assertions.assertEquals(reviewDto2.seriesId, longValue)
        Assertions.assertEquals(0, integerValues[0])
        Assertions.assertEquals(5, integerValues[1])
        then(reviewService).should()
            .reviewSeriesDtoPage(
                any(Long::class.java), any(Int::class.java), any(
                    Int::class.java
                )
            )
    }
}
