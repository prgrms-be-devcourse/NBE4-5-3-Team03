package com.example.Flicktionary.domain.review.service

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.review.dto.ReviewDto
import com.example.Flicktionary.domain.review.entity.Review
import com.example.Flicktionary.domain.review.repository.ReviewRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.BDDAssertions.catchThrowable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.*
import java.time.LocalDate
import java.util.*

@DisplayName("리뷰 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
    @Mock
    private lateinit var reviewRepository: ReviewRepository

    @Mock
    private lateinit var userAccountRepository: UserAccountRepository

    @Mock
    private lateinit var movieRepository: MovieRepository

    @Mock
    private lateinit var seriesRepository: SeriesRepository

    @InjectMocks
    private lateinit var reviewService: ReviewService


    // 변수 설정
    private val testUser = UserAccount(
        12L,
        "테스트용 유저",
        "test12345",
        "test@email.com",
        "테스트 유저",
        UserAccountType.USER
    )

    private lateinit var testMovie: Movie

    private lateinit var testSeries: Series

    private lateinit var reviewDto1: ReviewDto

    private lateinit var reviewDto2: ReviewDto

    @BeforeEach
    fun initValues() {
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
            id = 13L
            averageRating = 4.0
            ratingCount = 10
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
            id = 14L
            averageRating = 4.5
            ratingCount = 10
        }

        reviewDto1 = ReviewDto(
            id = null,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = testMovie.id,
            seriesId = null,
            rating = 5,
            content = "테스트용 리뷰 내용 (영화)",
        )

        reviewDto2 = ReviewDto(
            id = null,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = null,
            seriesId = testSeries.id,
            rating = 5,
            content = "테스트용 리뷰 내용 (드라마)",
        )
    }

    @Test
    @DisplayName("리뷰 작성")
    fun printReview() {
        given(userAccountRepository.findById(reviewDto1.userAccountId!!)).willReturn(Optional.of(testUser))
        given(movieRepository.findById(reviewDto1.movieId!!)).willReturn(Optional.of(testMovie))
        given(reviewRepository.save(any(Review::class.java)))
            .willReturn(
                Review(
                    id = 1L,
                    userAccount = testUser,
                    movie = testMovie,
                    series = null,
                    rating = reviewDto1.rating,
                    content = reviewDto1.content
                )
            )

        // 리뷰 생성 및 변수에 저장
        val reviewDto = reviewService.createReview(reviewDto1)

        /** 검증 /// */
        assertThat(reviewDto).isNotNull()
        assertThat(reviewDto.movieId).isNotNull()
        assertThat(reviewDto.seriesId).isNull()
        assertEquals(1L, reviewDto.id)
        assertEquals(reviewDto1.rating, reviewDto.rating)
        assertEquals(reviewDto1.content, reviewDto.content)
    }

    @Test
    @DisplayName("리뷰 작성 실패(리뷰 내용이 null이거나 빈 문자열일 때 예외 발생)")
    fun printReviewFailNull() {
        // 리뷰 내용이 비어있는 리뷰 생성
        val reviewEmpty = ReviewDto(
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = testMovie.id,
            seriesId = null,
            rating = 5,
            content = ""
        )

        // userAccountRepository.findById()가 호출될 때 testUser를 반환하도록 스텁 처리
        given(userAccountRepository.findById(testUser.id)).willReturn(Optional.of(testUser))

        val thrownOnEmpty = catchThrowable { reviewService.createReview(reviewEmpty) }

        /** 검증 /// */
        assertThat(thrownOnEmpty)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("리뷰 내용을 입력해주세요.")
    }

    @Test
    @DisplayName("리뷰 작성 실패(평점이 없을 때 예외 발생)")
    fun printReviewFailRating() {
        // 평점이 없는 리뷰 생성
        val reviewNoRating = ReviewDto(
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            movieId = testMovie.id,
            seriesId = null,
            rating = 0,
            content = "테스트용 리뷰 내용",
        )

        // userAccountRepository.findById()가 호출될 때 testUser를 반환하도록 스텁 처리
        given(userAccountRepository.findById(testUser.id)).willReturn(Optional.of(testUser))

        val thrown = catchThrowable { reviewService.createReview(reviewNoRating) }

        /** 검증 /// */
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("평점을 매겨주세요.")
    }


    @Test
    @DisplayName("리뷰 작성 시 영화 정보 업데이트")
    fun createReviewUpdateMovie() {
        val ratingCount = testMovie.ratingCount
        val averageRating = testMovie.averageRating
        given(userAccountRepository.findById(any()))
            .willReturn(Optional.of(testUser))
        given(movieRepository.findById(any()))
            .willReturn(Optional.of(testMovie))
        given(reviewRepository.save(any(Review::class.java)))
            .willReturn(reviewDto1.toEntity(testUser, testMovie, null))

        // 리뷰 생성 및 변수에 저장
        val review = reviewService.createReview(reviewDto1)

        val newRatingCount = ratingCount + 1
        val newAverageRating = (averageRating * ratingCount + review.rating) / newRatingCount

        assertThat(testMovie.ratingCount).isEqualTo(newRatingCount)
        assertThat(testMovie.averageRating).isEqualTo(newAverageRating)
        then(userAccountRepository).should().findById(reviewDto1.userAccountId!!)
        then(movieRepository).should().findById(reviewDto1.movieId!!)
        then(reviewRepository).should().save(any(Review::class.java))
    }

    @Test
    @DisplayName("리뷰 작성 시 시리즈 정보 업데이트")
    fun createReviewUpdateSeries() {
        val ratingCount = testSeries.ratingCount
        val averageRating = testSeries.averageRating
        given(userAccountRepository.findById(any())).willReturn(Optional.of(testUser))
        given(seriesRepository.findById(any())).willReturn(Optional.of(testSeries))
        given(reviewRepository.save(any(Review::class.java)))
            .willReturn(reviewDto2.toEntity(testUser, null, testSeries))

        // 리뷰 생성 및 변수에 저장
        val review = reviewService.createReview(reviewDto2)

        val newRatingCount = ratingCount + 1
        val newAverageRating = (averageRating * ratingCount + review.rating) / newRatingCount

        assertThat(testSeries.ratingCount).isEqualTo(newRatingCount)
        assertThat(testSeries.averageRating).isEqualTo(newAverageRating)
        then(userAccountRepository).should().findById(reviewDto2.userAccountId!!)
        then(seriesRepository).should().findById(reviewDto2.seriesId!!)
        then(reviewRepository).should().save(any(Review::class.java))
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    fun printAllReviews() {
        // 변수 입력
        val page = 0
        val size = 10
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("id").descending())

        // Page 객체 생성 리스트, pageable 객체, 전체 아이템 수를 넘김
        val reviewEntities = listOf(
            reviewDto1.toEntity(testUser, testMovie, null),
            reviewDto2.toEntity(testUser, null, testSeries)
        )
        val reviewPage: Page<Review> = PageImpl(reviewEntities, pageable, reviewEntities.size.toLong())

        given(reviewRepository.findAll(pageable)).willReturn(reviewPage)

        // 반환 타입 변경
        val reviewsPageDto: PageDto<ReviewDto> = reviewService.findAllReviews(page, size)
        val reviews = reviewsPageDto.items

        /** 검증 /// */
        assertThat(reviews).isNotEmpty().hasSize(2)
        assertThat(reviews).extracting<String>(ReviewDto::content)
            .containsExactlyInAnyOrder(reviewDto1.content, reviewDto2.content)
        assertThat(reviews).extracting<Long?>(ReviewDto::movieId)
            .contains(reviewDto1.movieId)
        assertThat(reviews).extracting<Long?>(ReviewDto::seriesId)
            .contains(reviewDto2.seriesId)

        // PageDto에 대한 추가적인 검증 (예: totalItems, totalPages 등)이 필요할 수 있습니다.
        assertThat(reviewsPageDto.totalItems).isEqualTo(2)
        assertThat(reviewsPageDto.totalPages).isEqualTo(1) // 2개의 아이템, 페이지 사이즈 10이므로 1페이지

        then(reviewRepository).should().findAll(pageable)
    }

    @Test
    @DisplayName("특정 영화의 리뷰 조회")
    fun printReviewByMovie() {
        given(
            reviewRepository.findByMovie_IdOrderByIdDesc(any(), any())
        )
            .willReturn(
                PageImpl(
                    listOf(reviewDto1.toEntity(testUser, testMovie, null)),
                    PageRequest.of(0, 5),
                    1L
                )
            )

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        val reviewDtoPageDto: PageDto<ReviewDto> = reviewService.reviewMovieDtoPage(
            reviewDto1.movieId!!, 0, 5
        )

        /** 검증 /// */
        assertThat(reviewDtoPageDto).isNotNull()
        assertThat(reviewDtoPageDto.items).hasSize(1)
        assertThat(reviewDtoPageDto.items).extracting<Long?>(ReviewDto::movieId)
            .contains(reviewDto1.movieId)
        assertEquals(1, reviewDtoPageDto.curPageNo)
        assertEquals(5, reviewDtoPageDto.pageSize)
        then(reviewRepository).should().findByMovie_IdOrderByIdDesc(
            any(), any()
        )
    }

    @Test
    @DisplayName("특정 드라마의 리뷰 조회")
    fun printReviewBySeries() {
        given(
            reviewRepository.findBySeries_IdOrderByIdDesc(
                any(), any()
            )
        )
            .willReturn(
                PageImpl(
                    listOf(reviewDto2.toEntity(testUser, null, testSeries)),
                    PageRequest.of(0, 5),
                    1L
                )
            )

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        val reviewDtoPageDto: PageDto<ReviewDto> = reviewService.reviewSeriesDtoPage(
            reviewDto2.seriesId!!, 0, 5
        )

        /** 검증 /// */
        assertThat(reviewDtoPageDto).isNotNull()
        assertThat(reviewDtoPageDto.items).hasSize(1)
        assertThat(reviewDtoPageDto.items).extracting<Long?>(ReviewDto::seriesId)
            .contains(reviewDto2.seriesId)
        assertEquals(1, reviewDtoPageDto.curPageNo)
        assertEquals(5, reviewDtoPageDto.pageSize)
        then(reviewRepository).should().findBySeries_IdOrderByIdDesc(
            any(), any()
        )
    }

    @DisplayName("리뷰 수정")
    @Test
    fun updateReview() {
        given(reviewRepository.findById(reviewDto1.id!!))
            .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)))

        // 수정할 리뷰 내용 변수에 저장
        val updatedReviewDto = ReviewDto(
            id = reviewDto1.id,
            userAccountId = reviewDto1.userAccountId,
            nickname = reviewDto1.nickname,
            movieId = reviewDto1.movieId,
            seriesId = null,
            rating = 4,
            content = "(테스트)수정된 리뷰 내용"
        )

        // 수정
        val result = reviewService.updateReview(reviewDto1.id!!, updatedReviewDto)

        /** 검증 /// */
        assertThat(result).isNotNull()
        assertEquals(4, result.rating)
        assertEquals("(테스트)수정된 리뷰 내용", result.content)
        then(reviewRepository).should().findById(reviewDto1.id!!)
    }

    @Test
    @DisplayName("리뷰 수정 시 영화 정보 업데이트")
    fun updateReviewUpdateMovie() {
        val ratingCount = testMovie.ratingCount
        val averageRating = testMovie.averageRating
        // 수정할 리뷰 내용 변수에 저장
        val updatedReviewDto = ReviewDto(
            id = reviewDto1.id,
            userAccountId = reviewDto1.userAccountId,
            nickname = reviewDto1.nickname,
            movieId = reviewDto1.movieId,
            seriesId = reviewDto1.seriesId,
            rating = 4,
            content = "(테스트)수정된 리뷰 내용"
        )
        given(reviewRepository.findById(reviewDto1.id!!))
            .willReturn(Optional.of(updatedReviewDto.toEntity(testUser, testMovie, null)))

        // 수정
        val review = reviewService.updateReview(reviewDto1.id!!, updatedReviewDto)

        val newAverageRating = (averageRating * ratingCount - updatedReviewDto.rating + review.rating) / ratingCount

        assertThat(testMovie.ratingCount).isEqualTo(ratingCount)
        assertThat(testMovie.averageRating).isEqualTo(newAverageRating)
        then(reviewRepository).should().findById(reviewDto1.id!!)
    }

    @Test
    @DisplayName("리뷰 수정 시 시리즈 정보 업데이트")
    fun updateReviewUpdateSeries() {
        val ratingCount = testSeries.ratingCount
        val averageRating = testSeries.averageRating
        // 수정할 리뷰 내용 변수에 저장
        val updatedReviewDto = ReviewDto(
            id = reviewDto2.id,
            userAccountId = reviewDto2.userAccountId,
            nickname = reviewDto2.nickname,
            movieId = reviewDto2.movieId,
            seriesId = reviewDto2.seriesId,
            rating = 4,
            content = "(테스트)수정된 리뷰 내용"
        )
        given(reviewRepository.findById(reviewDto2.id!!))
            .willReturn(Optional.of(updatedReviewDto.toEntity(testUser, null, testSeries)))

        // 수정
        val review = reviewService.updateReview(reviewDto2.id!!, updatedReviewDto)

        val newAverageRating = (averageRating * ratingCount - updatedReviewDto.rating + review.rating) / ratingCount

        assertThat(testSeries.ratingCount).isEqualTo(ratingCount)
        assertThat(testSeries.averageRating).isEqualTo(newAverageRating)
        then(reviewRepository).should().findById(reviewDto2.id!!)
    }

    @DisplayName("존재하지 않는 리뷰 수정")
    @Test
    fun updateNonexistentReview() {
        given(reviewRepository.findById(1234L)).willReturn(Optional.empty())

        // 수정
        val thrown = catchThrowable { reviewService.updateReview(1234L, reviewDto1) }

        /** 검증 /// */
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("1234번 리뷰를 찾을 수 없습니다.")
        then(reviewRepository).should().findById(1234L)
    }

    @DisplayName("리뷰 삭제")
    @Test
    fun deleteReview() {
        given(reviewRepository.findById(reviewDto1.id!!))
            .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)))
        doNothing().`when`(reviewRepository).deleteById(any())

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto1.id!!)

        /** 검증 /// */
        then(reviewRepository).should().findById(reviewDto1.id!!)
        then(reviewRepository).should().delete(any())
    }

    @Test
    @DisplayName("리뷰 삭제 시 영화 정보 업데이트")
    fun deleteReviewUpdateMovie() {
        val ratingCount = testMovie.ratingCount
        val averageRating = testMovie.averageRating
        given(reviewRepository.findById(reviewDto1.id!!))
            .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)))
        doNothing().`when`(reviewRepository).delete(any())

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto1.id!!)

        val newRatingCount = ratingCount - 1
        val newAverageRating = (averageRating * ratingCount - reviewDto1.rating) / newRatingCount

        assertThat(testMovie.ratingCount).isEqualTo(newRatingCount)
        assertThat(testMovie.averageRating).isEqualTo(newAverageRating)
        then(reviewRepository).should().findById(reviewDto1.id!!)
        then(reviewRepository).should().delete(any())
    }

    @Test
    @DisplayName("리뷰 삭제 시 시리즈 정보 업데이트")
    fun deleteReviewUpdateSeries() {
        val ratingCount = testSeries.ratingCount
        val averageRating = testSeries.averageRating
        given(reviewRepository.findById(reviewDto2.id!!))
            .willReturn(Optional.of(reviewDto2.toEntity(testUser, null, testSeries)))
        doNothing().`when`(reviewRepository).delete(any())

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto2.id!!)

        val newRatingCount = ratingCount - 1
        val newAverageRating = (averageRating * ratingCount - reviewDto2.rating) / newRatingCount

        assertThat(testSeries.ratingCount).isEqualTo(newRatingCount)
        assertThat(testSeries.averageRating).isEqualTo(newAverageRating)
        then(reviewRepository).should().findById(reviewDto2.id!!)
        then(reviewRepository).should().delete(any())
    }

    @DisplayName("존재하지 않는 리뷰 삭제")
    @Test
    fun deleteNonexistentReview() {
        given(reviewRepository.findById(reviewDto1.id!!)).willReturn(Optional.empty())

        // 리뷰 삭제
        val thrown = catchThrowable { reviewService.deleteReview(reviewDto1.id!!) }

        /** 검증 /// */
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${reviewDto1.id}번 리뷰를 찾을 수 없습니다.")
    }
}
