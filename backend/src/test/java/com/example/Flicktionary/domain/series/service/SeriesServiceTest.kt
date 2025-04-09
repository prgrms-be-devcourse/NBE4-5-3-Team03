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
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.util.*

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

        BDDMockito.given(
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

        Assertions.assertThat(series).isNotNull()
        Assertions.assertThat(series.content.size).isGreaterThan(0)
        org.junit.jupiter.api.Assertions.assertEquals(123L, series.content[0].id)
        org.junit.jupiter.api.Assertions.assertEquals("testTitle", series.content[0].title)
    }

    @Test
    @DisplayName("Series 목록 조회 - 평점 내림차순 정렬")
    fun testGetSeriesSortByRating() {
        val keyword = ""
        val sortBy = "rating"
        val page = 1
        val pageSize = 10

        BDDMockito.given(
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

        Assertions.assertThat(series).isNotNull()
        Assertions.assertThat(series.content.size).isGreaterThan(0)
    }

    @Test
    @DisplayName("Series 목록 조회 - 리뷰 개수 내림차순 정렬")
    fun testGetSeriesSortByRatingCount() {
        val keyword = ""
        val sortBy = "ratingCount"
        val page = 1
        val pageSize = 10

        BDDMockito.given(
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


        Assertions.assertThat(series).isNotNull()
    }

    @Test
    @DisplayName("Series 목록 조회 - 검색")
    fun testGetSeriesForSearch() {
        val keyword = "The"
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        BDDMockito.given(
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

        Assertions.assertThat(series).isNotNull()
    }

    @Test
    @DisplayName("Series 목록 조회 - 잘못된 정렬 방식 예외 처리")
    fun testGetSeriesSortByFail() {
        val keyword = "The"
        val sortBy = "invalidSortParameter"
        val page = 1
        val pageSize = 10

        val thrown = Assertions.catchThrowable {
            seriesService.getSeries(
                keyword,
                page,
                pageSize,
                sortBy
            )
        }

        Assertions.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("잘못된 정렬 기준입니다.")
    }

    @Test
    @DisplayName("Series 상세 정보 조회 성공 테스트")
    fun testGetSeriesDetail_Success() {
        // given
        val seriesId = 123L
        BDDMockito.given(seriesRepository.findByIdWithCastsAndDirector(seriesId))
            .willReturn(series)

        // when
        val response = seriesService.getSeriesDetail(seriesId)

        // then
        org.junit.jupiter.api.Assertions.assertNotNull(response)
        org.junit.jupiter.api.Assertions.assertEquals(seriesId, response.id)
        org.junit.jupiter.api.Assertions.assertEquals("testTitle", response.title)
        BDDMockito.then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId)
    }

    @Test
    @DisplayName("Series 상세 정보 조회 실패 테스트 (존재하지 않는 ID)")
    fun testGetSeriesDetail_Fail_NotFound() {
        // given
        val seriesId = 999L // 존재하지 않는 ID
        BDDMockito.given(seriesRepository.findByIdWithCastsAndDirector(seriesId)).willReturn(null)

        // when
        val thrown = Assertions.catchThrowable {
            seriesService.getSeriesDetail(
                seriesId
            )
        }

        // then
        Assertions.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${seriesId}번 시리즈를 찾을 수 없습니다.")
        BDDMockito.then(seriesRepository).should().findByIdWithCastsAndDirector(seriesId)
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

        val genre1 = Genre(1L, "Action")
        val genre2 = Genre(2L, "Drama")
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
        Mockito.`when`(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        Mockito.`when`(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        Mockito.`when`(directorRepository.findById(1L)).thenReturn(Optional.of(director))
        Mockito.`when`(
            seriesRepository.save(
                ArgumentMatchers.any(
                    Series::class.java
                )
            )
        ).thenReturn(savedSeries)

        val response = seriesService.createSeries(request)

        // then
        org.junit.jupiter.api.Assertions.assertNotNull(response)
        org.junit.jupiter.api.Assertions.assertEquals(request.title, response.title)
        org.junit.jupiter.api.Assertions.assertEquals(request.overview, response.plot)
        org.junit.jupiter.api.Assertions.assertEquals(request.posterPath, response.posterPath)
        org.junit.jupiter.api.Assertions.assertEquals("Action", response.genres[0].name)
        org.junit.jupiter.api.Assertions.assertEquals("Drama", response.genres[1].name)
        org.junit.jupiter.api.Assertions.assertEquals("name", response.casts[0].actor.name)
        org.junit.jupiter.api.Assertions.assertEquals("characterName", response.casts[0].characterName)
        org.junit.jupiter.api.Assertions.assertEquals("name", response.director?.name)
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

        val genre1 = Genre(1L, "Action")
        val genre2 = Genre(2L, "Drama")
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
        Mockito.`when`(seriesRepository.findById(id)).thenReturn(Optional.of(series))
        Mockito.`when`(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        Mockito.`when`(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        Mockito.`when`(directorRepository.findById(1L)).thenReturn(Optional.of(director))

        val response = seriesService.updateSeries(id, request)

        // then
        org.junit.jupiter.api.Assertions.assertNotNull(response)
        org.junit.jupiter.api.Assertions.assertEquals(request.title, response.title)
        org.junit.jupiter.api.Assertions.assertEquals(request.overview, response.plot)
        org.junit.jupiter.api.Assertions.assertEquals(request.posterPath, response.posterPath)
        org.junit.jupiter.api.Assertions.assertEquals("Action", response.genres[0].name)
        org.junit.jupiter.api.Assertions.assertEquals("Drama", response.genres[1].name)
        org.junit.jupiter.api.Assertions.assertEquals("name", response.casts[0].actor.name)
        org.junit.jupiter.api.Assertions.assertEquals("characterName", response.casts[0].characterName)
        org.junit.jupiter.api.Assertions.assertEquals("name", response.director?.name)
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
        Mockito.`when`(seriesRepository.findById(id)).thenReturn(Optional.empty())
        val thrown = Assertions.catchThrowable {
            seriesService.updateSeries(
                id,
                request
            )
        }

        // then
        Assertions.assertThat(thrown)
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
        Mockito.`when`(seriesRepository.findById(id)).thenReturn(Optional.of(series))
        seriesService.deleteSeries(id)

        // then
        Mockito.verify(seriesRepository).delete(series)
    }

    @Test
    @DisplayName("시리즈 삭제 - 실패 - 없는 시리즈")
    fun deleteSeries2() {
        // given
        val id = 1L

        // when
        Mockito.`when`(seriesRepository.findById(id)).thenReturn(Optional.empty())
        val thrown = Assertions.catchThrowable { seriesService.deleteSeries(id) }

        // then
        Assertions.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${id}번 시리즈를 찾을 수 없습니다.")
    }
}
