package com.example.Flicktionary.domain.series.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.series.dto.SeriesRequest
import com.example.Flicktionary.domain.series.dto.SeriesRequest.SeriesCastRequest
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.entity.SeriesCast
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.tmdb.service.TmdbService
import com.example.Flicktionary.global.exception.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
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
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("시리즈 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class SeriesServiceTest {
    @Mock
    private lateinit var seriesRepository: SeriesRepository

    @Mock
    private lateinit var genreRepository: GenreRepository

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var directorRepository: DirectorRepository

    @Mock
    private lateinit var tmdbService: TmdbService

    @InjectMocks
    private lateinit var seriesService: SeriesService

    private lateinit var series: Series

    @BeforeEach
    fun setUp() {
        series = Series(
            "testTitle", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        )
        series.id = 123L
    }

    @Test
    @DisplayName("Series 목록 조회 - id 오름차순 정렬")
    fun testGetSeriesSortById() {
        val keyword = ""
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        given(
            seriesRepository.findByTitleLike(
                keyword, PageRequest.of(page - 1, pageSize, Sort.by("id").ascending())
            )
        )
            .willReturn(
                PageImpl(
                    listOf(series),
                    PageRequest.of(page - 1, pageSize, Sort.by("id").ascending()),
                    1
                )
            )

        val series = seriesService.getSeries(keyword, page, pageSize, sortBy)

        assertThat(series).isNotNull()
        assertThat(series.content.size).isGreaterThan(0)
        assertEquals(123L, series.content[0].id)
        assertEquals("testTitle", series.content[0].title)
    }

    @Test
    @DisplayName("Series 목록 조회 - 평점 내림차순 정렬")
    fun testGetSeriesSortByRating() {
        val keyword = ""
        val sortBy = "rating"
        val page = 1
        val pageSize = 10

        given(
            seriesRepository.findByTitleLike(
                keyword, PageRequest.of(page - 1, pageSize, Sort.by("averageRating").descending())
            )
        )
            .willReturn(
                PageImpl(
                    listOf(series),
                    PageRequest.of(page - 1, pageSize, Sort.by("averageRating").descending()),
                    1
                )
            )

        val series = seriesService.getSeries(keyword, page, pageSize, sortBy)

        assertThat(series).isNotNull()
        assertThat(series.content.size).isGreaterThan(0)
    }

    @Test
    @DisplayName("Series 목록 조회 - 리뷰 개수 내림차순 정렬")
    fun testGetSeriesSortByRatingCount() {
        val keyword = ""
        val sortBy = "ratingCount"
        val page = 1
        val pageSize = 10

        given(
            seriesRepository.findByTitleLike(
                keyword, PageRequest.of(page - 1, pageSize, Sort.by("ratingCount").descending())
            )
        )
            .willReturn(
                PageImpl(
                    listOf(series),
                    PageRequest.of(page - 1, pageSize, Sort.by("ratingCount").descending()),
                    1
                )
            )

        val series = seriesService.getSeries(keyword, page, pageSize, sortBy)


        assertThat(series).isNotNull()
    }

    @Test
    @DisplayName("Series 목록 조회 - 검색")
    fun testGetSeriesForSearch() {
        val keyword = "The"
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        given(
            seriesRepository.findByTitleLike(
                keyword, PageRequest.of(page - 1, pageSize, Sort.by("id").ascending())
            )
        )
            .willReturn(
                PageImpl(
                    listOf(series),
                    PageRequest.of(page - 1, pageSize, Sort.by("id").ascending()),
                    1
                )
            )

        val series = seriesService.getSeries(keyword, page, pageSize, sortBy)

        assertThat(series).isNotNull()
    }

    @Test
    @DisplayName("Series 목록 조회 - 잘못된 정렬 방식 예외 처리")
    fun testGetSeriesSortByFail() {
        val keyword = "The"
        val sortBy = "invalidSortParameter"
        val page = 1
        val pageSize = 10

        val thrown = catchThrowable {
            seriesService.getSeries(
                keyword,
                page,
                pageSize,
                sortBy
            )
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("잘못된 정렬 기준입니다.")
    }

    @Test
    @DisplayName("Series 상세 정보 조회 성공 테스트")
    fun testGetSeriesDetail_Success() {
        // given
        val seriesId = 123L
        given(seriesRepository.findByIdWithCastsAndDirector(seriesId))
            .willReturn(series)

        // when
        val response = seriesService.getSeriesDetail(seriesId)

        // then
        assertNotNull(response)
        assertEquals(seriesId, response.id)
        assertEquals("testTitle", response.title)
        then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId)
    }

    @Test
    @DisplayName("Series 상세 정보 조회 실패 테스트 (존재하지 않는 ID)")
    fun testGetSeriesDetail_Fail_NotFound() {
        // given
        val seriesId = 999L // 존재하지 않는 ID
        given(seriesRepository.findByIdWithCastsAndDirector(seriesId)).willReturn(null)

        // when
        val thrown = catchThrowable {
            seriesService.getSeriesDetail(
                seriesId
            )
        }

        // then
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${seriesId}번 시리즈를 찾을 수 없습니다.")
        then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId)
    }

    @Test
    @DisplayName("시리즈 생성 - 성공")
    fun createSeries1() {
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

        val genre1 = Genre("Action")
        val genre2 = Genre("Drama")
        val actor = Actor("name", null)
        val director = Director("name", "PosterPath")

        val savedSeries = Series(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath",
            10,
            "productionCountry",
            "productionCompany"
        )
        savedSeries.id = 1L
        savedSeries.genres.addAll(listOf(genre1, genre2))
        savedSeries.casts.add(SeriesCast(savedSeries, actor, "characterName"))
        savedSeries.director = director

        // when
        whenever(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        whenever(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        whenever(directorRepository.findById(1L)).thenReturn(Optional.of(director))
        whenever(seriesRepository.save(any<Series>())).thenReturn(savedSeries)

        val response = seriesService.createSeries(request)

        // then
        assertNotNull(response)
        assertEquals(request.title, response.title)
        assertEquals(request.overview, response.plot)
        assertEquals(request.posterPath, response.posterPath)
        assertEquals("Action", response.genres[0].name)
        assertEquals("Drama", response.genres[1].name)
        assertEquals("name", response.casts[0].actor.name)
        assertEquals("characterName", response.casts[0].characterName)
        assertEquals("name", response.director?.name)
    }

    @Test
    @DisplayName("시리즈 수정 - 성공")
    fun updateSeries1() {
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

        val genre1 = Genre("Action")
        val genre2 = Genre("Drama")
        val actor = Actor("name", null)
        val director = Director("name", "PosterPath")

        val series = Series(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath",
            10,
            "productionCountry",
            "productionCompany"
        )
        series.id = id

        // when
        whenever(seriesRepository.findById(id)).thenReturn(Optional.of(series))
        whenever(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        whenever(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        whenever(directorRepository.findById(1L)).thenReturn(Optional.of(director))

        val response = seriesService.updateSeries(id, request)

        // then
        assertNotNull(response)
        assertEquals(request.title, response.title)
        assertEquals(request.overview, response.plot)
        assertEquals(request.posterPath, response.posterPath)
        assertEquals("Action", response.genres[0].name)
        assertEquals("Drama", response.genres[1].name)
        assertEquals("name", response.casts[0].actor.name)
        assertEquals("characterName", response.casts[0].characterName)
        assertEquals("name", response.director?.name)
    }

    @Test
    @DisplayName("시리즈 수정 - 실패 - 없는 시리즈")
    fun updateSeries2() {
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

        // when
        whenever(seriesRepository.findById(id)).thenReturn(Optional.empty())
        val thrown = catchThrowable {
            seriesService.updateSeries(
                id,
                request
            )
        }

        // then
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${id}번 시리즈를 찾을 수 없습니다.")
    }

    @Test
    @DisplayName("시리즈 삭제 - 성공")
    fun deleteSeries1() {
        // given
        val id = 1L
        val series = Series(
            "title",
            "overview",
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2023, 1, 1),
            "status",
            "posterPath",
            10,
            "productionCountry",
            "productionCompany"
        )
        series.id = id

        // when
        whenever(seriesRepository.findById(id)).thenReturn(Optional.of(series))
        seriesService.deleteSeries(id)

        // then
        verify(seriesRepository).delete(series)
    }

    @Test
    @DisplayName("시리즈 삭제 - 실패 - 없는 시리즈")
    fun deleteSeries2() {
        // given
        val id = 1L

        // when
        whenever(seriesRepository.findById(id)).thenReturn(Optional.empty())
        val thrown = catchThrowable { seriesService.deleteSeries(id) }

        // then
        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${id}번 시리즈를 찾을 수 없습니다.")
    }
}
