package com.example.Flicktionary.domain.actor.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.entity.MovieCast
import com.example.Flicktionary.domain.movie.repository.MovieCastRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.entity.SeriesCast
import com.example.Flicktionary.domain.series.repository.SeriesCastRepository
import org.assertj.core.api.Assertions.assertThat
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
import java.time.LocalDate
import java.util.*

@DisplayName("배우 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class ActorServiceTest {
    @Mock
    private lateinit var actorRepository: ActorRepository
    @Mock
    private lateinit var movieCastRepository: MovieCastRepository
    @Mock
    private lateinit var seriesCastRepository: SeriesCastRepository
    @InjectMocks
    private lateinit var actorService: ActorService

    private lateinit var actor1: Actor
    private lateinit var actor2: Actor
    private lateinit var actor3: Actor
    private lateinit var movie: Movie
    private lateinit var series: Series

    @BeforeEach
    fun setUp() {
        actor1 = Actor("actor1", "test1.png")
        actor2 = Actor("actor2", "test2.png")
        actor3 = Actor("actor3", "test3.png")

        movie = Movie("movie", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", "")
        movie.id = 1L

        series = Series("series", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", "")
        series.id = 1L
    }


    @Test
    @DisplayName("특정 배우 조회 - 성공")
    fun getActorById1() {
        given(actorRepository.findById(1L)).willReturn(Optional.of(actor1))

        val result = actorService.getActorById(1L)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(actor1)
        then(actorRepository).should().findById(1L)
    }

    @Test
    @DisplayName("배우가 출연한 영화 리스트 조회 - 성공")
    fun getMoviesByActorId1() {
        // Given
        val movieCast = MovieCast(movie, actor1, "name")
        movieCast.id = 1L
        given(movieCastRepository.findMoviesByActorId(1L)).willReturn(listOf(movieCast))

        // When
        val result = actorService.getMoviesByActorId(1L)

        // Then
        assertEquals(movie.title, result.first().title)
        assertEquals(movie.releaseDate, result.first().releaseDate)
        then(movieCastRepository).should().findMoviesByActorId(1L)
    }

    @Test
    @DisplayName("배우가 출연한 시리즈 리스트 조회 - 성공")
    fun getSeriesByActorId1() {
        // Given
        val seriesCast = SeriesCast(series, actor1, "name")
        seriesCast.id = 1L
        given(seriesCastRepository.findSeriesByActorId(1L)).willReturn(listOf(seriesCast))

        // When
        val result = actorService.getSeriesByActorId(1L)

        // Then
        assertEquals(series.title, result.first().title)
        assertEquals(series.releaseEndDate, result.first().releaseEndDate)
        then(seriesCastRepository).should().findSeriesByActorId(1L)
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 기본")
    fun getActors1() {
        val keyword = ""
        val page = 1
        val pageSize = 10

        val pageable = PageRequest.of(page - 1, pageSize)
        val captor = argumentCaptor<Pageable>()

        val mockPage = PageImpl(listOf(actor1, actor2, actor3), pageable, 3)

        given(actorRepository.findByNameLike(any<String>(), captor.capture())).willReturn(mockPage)

        val result = actorService.getActors(keyword, page, pageSize)
        val captured = captor.firstValue

        assertThat(result).isNotNull()
        assertThat(result.content.size).isEqualTo(3)
        // Pageable 검증
        assertEquals(pageSize, captured.pageSize)
        assertEquals(page - 1, captured.pageNumber)

        // 배우 데이터 검증
        assertEquals(actor1.id, result.content.first().id)
        assertEquals(actor1.profilePath, result.content.first().profilePath)

        then(actorRepository).should().findByNameLike(any<String>(), any<Pageable>())
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 검색")
    fun getActors2() {
        val keyword = "a c t o r"
        val page = 1
        val pageSize = 10

        val pageable = PageRequest.of(page - 1, pageSize)
        val captor = argumentCaptor<Pageable>()

        val mockPage = PageImpl(listOf(actor1, actor2, actor3), pageable, 3)

        given(actorRepository.findByNameLike(any<String>(), captor.capture())).willReturn(mockPage)

        val result = actorService.getActors(keyword, page, pageSize)
        val captured = captor.firstValue

        assertThat(result).isNotNull()
        assertThat(result.content.size).isEqualTo(3)
        // Pageable 검증
        assertEquals(pageSize, captured.pageSize)
        assertEquals(page - 1, captured.pageNumber)

        // 배우 데이터 검증
        assertEquals(actor1.id, result.content.first().id)
        assertEquals(actor1.profilePath, result.content.first().profilePath)

        then(actorRepository).should().findByNameLike(any<String>(), any<Pageable>())
    }

    @Test
    @DisplayName("배우 목록 조회 - 성공 - 페이징")
    fun getActors3() {
        val keyword = ""
        val page = 2
        val pageSize = 2

        val pageable = PageRequest.of(page - 1, pageSize)
        val captor = argumentCaptor<Pageable>()

        val mockPage = PageImpl(listOf(actor3), pageable, 1)

        given(actorRepository.findByNameLike(any<String>(), captor.capture())).willReturn(mockPage)

        val result = actorService.getActors(keyword, page, pageSize)
        val captured = captor.firstValue

        assertThat(result).isNotNull()
        assertThat(result.content.size).isEqualTo(1)
        // Pageable 검증
        assertEquals(pageSize, captured.pageSize)
        assertEquals(page - 1, captured.pageNumber)

        // 배우 데이터 검증
        assertEquals(actor3.id, result.content.first().id)
        assertEquals(actor3.profilePath, result.content.first().profilePath)

        then(actorRepository).should().findByNameLike(any<String>(), any<Pageable>())
    }
}