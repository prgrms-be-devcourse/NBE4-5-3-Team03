package com.example.Flicktionary.domain.favorite.service

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto
import com.example.Flicktionary.domain.favorite.entity.ContentType
import com.example.Flicktionary.domain.favorite.entity.Favorite
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.util.*

@DisplayName("즐겨찾기 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class FavoriteServiceTest {
    @Mock
    private lateinit var favoriteRepository: FavoriteRepository

    @Mock
    private lateinit var userAccountRepository: UserAccountRepository

    @Mock
    private lateinit var movieRepository: MovieRepository

    @Mock
    private lateinit var seriesRepository: SeriesRepository

    @InjectMocks
    private lateinit var favoriteService: FavoriteService

    private lateinit var testUser: UserAccount
    private lateinit var testMovie1: Movie
    private lateinit var testMovie2: Movie
    private lateinit var testSeries: Series
    private lateinit var favorite1: FavoriteDto
    private lateinit var favorite2: FavoriteDto

    private val movieCount = 2L
    private val seriesCount = 1L

    @BeforeEach
    fun setUp() {
        testUser = UserAccount(
            123L,
            "tester1234",
            "tester1234",
            "tester1234@email.com",
            "tester1234",
            UserAccountType.USER
        )

        testMovie1 = Movie(
            "test movie1", "test movie1",
            LocalDate.of(2024, 1, 1), "test movie1", "test movie1",
            100, "KR", "test movie1"
        )
        testMovie1.id = 95L
        testMovie1.averageRating = 4.0
        testMovie1.ratingCount = 15

        testMovie2 = Movie(
            "test movie2", "test movie2",
            LocalDate.of(2024, 1, 1), "test movie2", "test movie2",
            100, "KR", "test movie2"
        )
        testMovie2.id = 59L
        testMovie2.averageRating = 3.2
        testMovie2.ratingCount = 151

        testSeries = Series(
            "test series1", "test series1",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1),
            "test series1", "test series1", 1, "KR", "test series1"
        )
        testSeries.id = 101L
        testSeries.averageRating = 4.3
        testSeries.ratingCount = 5

        favorite1 = FavoriteDto(
            id = 0L,
            userId = testUser.id!!,
            contentType = ContentType.MOVIE,
            contentId = testMovie1.id
        )

        favorite2 = FavoriteDto(
            id = 0L,
            userId = testUser.id!!,
            contentType = ContentType.SERIES,
            contentId = testSeries.id
        )
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공")
    fun createFavorite_Success() {
        val captor = argumentCaptor<Favorite>()

        given(userAccountRepository.findById(testUser.id!!))
            .willReturn(Optional.of(testUser))
        given(
            favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.userId,
                favorite1.contentType,
                favorite1.contentId
            )
        )
            .willReturn(false)
        given(movieRepository.existsById(favorite1.contentId)).willReturn(true)
        given(favoriteRepository.save(captor.capture())).willReturn(favorite1.toEntity(testUser))

        val result = favoriteService.createFavorite(favorite1)
        val captured = captor.firstValue

        assertThat(result).isNotNull()
        assertEquals(favorite1.userId, captured.userAccount.id)
        assertEquals(favorite1.contentType, captured.contentType)
        assertEquals(favorite1.contentId, captured.contentId)
        assertEquals(favorite1.userId, result.userId)
        assertEquals(favorite1.contentType, result.contentType)
        assertEquals(favorite1.contentId, result.contentId)

        then(userAccountRepository).should().findById(testUser.id!!)
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
            favorite1.userId,
            favorite1.contentType,
            favorite1.contentId
        )
        then(movieRepository).should().existsById(favorite1.contentId)
        then(favoriteRepository).should().save(any())
    }

    @Test
    @DisplayName("중복된 즐겨찾기 추가 시 예외 발생")
    fun createFavorite_Duplicate_ThrowsException() {
        given(userAccountRepository.findById(testUser.id!!))
            .willReturn(Optional.of(testUser))
        given(
            favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.userId,
                favorite1.contentType,
                favorite1.contentId
            )
        )
            .willReturn(true)

        val thrown = catchThrowable {
            favoriteService.createFavorite(favorite1)
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("이미 즐겨찾기에 추가된 항목입니다.")

        then(userAccountRepository).should().findById(testUser.id!!)
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
            favorite1.userId,
            favorite1.contentType,
            favorite1.contentId
        )
    }

    @Test
    @DisplayName("존재하지 않는 User 추가 시 예외 발생")
    fun createFavorite_UserNotFound_ThrowsException() {
        val favoriteDto = favorite1.copy(userId = 9999L)

        given(userAccountRepository.findById(favoriteDto.userId)).willReturn(Optional.empty())

        val thrown = catchThrowable {
            favoriteService.createFavorite(favoriteDto)
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${favoriteDto.userId}번 유저를 찾을 수 없습니다.")

        then(userAccountRepository).should().findById(favoriteDto.userId)
    }

    @Test
    @DisplayName("존재하지 않는 Content 추가 시 예외 발생")
    fun createFavorite_ContentNotFound_ThrowsException() {
        val favoriteDto = favorite1.copy(contentId = 999999999L)

        given(userAccountRepository.findById(any()))
            .willReturn(Optional.of(testUser))
        given(
            favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                any(),
                any(),
                any()
            )
        ).willReturn(false)
        given(movieRepository.existsById(any())).willReturn(false)

        val thrown = catchThrowable {
            favoriteService.createFavorite(favoriteDto)
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${favoriteDto.contentId}번 컨텐츠를 찾을 수 없습니다.")

        then(userAccountRepository).should().findById(any())
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
            any(),
            any(),
            any()
        )
        then(movieRepository).should().existsById(any())
    }

    @DisplayName("즐겨찾기 목록 조회 - ID 정렬 및 페이징")
    @Test
    fun getUserFavorites_SortById() {
        // Given
        val page = 1
        val pageSize = 5
        val sortBy = "id"
        val direction = "desc"
        val captor = argumentCaptor<Pageable>()

        given(userAccountRepository.existsById(favorite1.userId)).willReturn(true)
        given(
            favoriteRepository.findAllByUserAccountIdWithContent(
                any(), captor.capture()
            )
        )
            .willReturn(
                PageImpl(
                    listOf(favorite1.toEntity(testUser), favorite2.toEntity(testUser)),
                    PageRequest.of(
                        page - 1,
                        pageSize,
                        Sort.by(
                            Sort.Direction.fromString(direction),
                            "id"
                        )
                    ),
                    10
                )
            )

        // When
        val favorites = favoriteService.getUserFavorites(
            testUser.id!!, page, pageSize, sortBy, direction
        )
        val captured = captor.firstValue

        // Then
        assertThat(favorites).isNotNull()
        assertThat(favorites.items).hasSize(2)
        assertThat(favorites.items).flatExtracting<RuntimeException>(FavoriteDto::userId)
            .contains(favorite1.userId, favorite2.userId)
        assertEquals(page - 1, captured.pageNumber)
        assertEquals(pageSize, captured.pageSize)
        assertEquals(
            Sort.by(Sort.Direction.fromString(direction), "id"),
            captured.sort
        )

        then(userAccountRepository).should().existsById(favorite1.userId)
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(
            any(), any()
        )
    }

    @DisplayName("즐겨찾기 목록 조회 - 평점 정렬")
    @Test
    fun getUserFavorites_SortByRating() {
        // Given
        val page = 1
        val pageSize = 2
        val sortBy = "rating"
        val direction = "asc"
        val captor = argumentCaptor<Pageable>()

        given(userAccountRepository.existsById(favorite1.userId)).willReturn(true)
        given(
            favoriteRepository.findAllByUserAccountIdWithContent(
                any(), captor.capture()
            )
        )
            .willReturn(
                PageImpl(
                    emptyList(),
                    PageRequest.of(
                        page - 1,
                        pageSize,
                        Sort.by(
                            Sort.Direction.fromString(direction),
                            "movie.averageRating",
                            "series.averageRating"
                        )
                    ),
                    10
                )
            )

        // When
        favoriteService.getUserFavorites(testUser.id!!, page, pageSize, sortBy, direction)
        val captured = captor.firstValue

        // Then
        assertEquals(
            Sort.by(
                Sort.Direction.fromString(direction), "movie.averageRating",
                "series.averageRating"
            ), captured.sort
        )

        then(userAccountRepository).should().existsById(favorite1.userId)
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(
            any(), any()
        )
    }

    @DisplayName("즐겨찾기 목록 조회 - 리뷰 개수 정렬")
    @Test
    fun getUserFavorites_SortByReviews() {
        // Given
        val page = 1
        val pageSize = 2
        val sortBy = "reviews"
        val direction = "desc"
        val captor = argumentCaptor<Pageable>()

        given(userAccountRepository.existsById(favorite1.userId)).willReturn(true)
        given(
            favoriteRepository.findAllByUserAccountIdWithContent(
                any(), captor.capture()
            )
        )
            .willReturn(
                PageImpl(
                    emptyList(),
                    PageRequest.of(
                        page - 1,
                        pageSize,
                        Sort.by(
                            Sort.Direction.fromString(direction),
                            "movie.ratingCount",
                            "series.ratingCount"
                        )
                    ),
                    10
                )
            )

        // When
        favoriteService.getUserFavorites(testUser.id!!, page, pageSize, sortBy, direction)
        val captured = captor.firstValue

        // Then
        assertEquals(
            Sort.by(
                Sort.Direction.fromString(direction), "movie.ratingCount",
                "series.ratingCount"
            ), captured.sort
        )
        then(userAccountRepository).should().existsById(favorite1.userId)
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(
            any(), any()
        )
    }

    @DisplayName("잘못된 정렬 기준 입력 시 예외 발생")
    @Test
    fun getUserFavorites_InvalidSortBy_ThrowsException() {
        val page = 1
        val pageSize = 5
        val sortBy = "unknown"
        val direction = "descasd"

        given(userAccountRepository.existsById(favorite1.userId)).willReturn(true)

        val thrown = catchThrowable {
            favoriteService.getUserFavorites(
                testUser.id!!,
                page,
                pageSize,
                sortBy,
                direction
            )
        }

        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage(
                "Invalid value '${direction}' for orders given; Has to be either 'desc' or 'asc' (case insensitive)"
            )

        then(userAccountRepository).should().existsById(favorite1.userId)
    }
}