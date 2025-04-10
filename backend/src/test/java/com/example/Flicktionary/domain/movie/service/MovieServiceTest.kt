package com.example.Flicktionary.domain.movie.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.movie.dto.MovieRequest
import com.example.Flicktionary.domain.movie.dto.MovieRequest.MovieCastRequest
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.entity.MovieCast
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.tmdb.service.TmdbService
import com.example.Flicktionary.global.exception.ServiceException
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Assertions
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

@DisplayName("영화 서비스 테스트")
@ExtendWith(MockitoExtension::class)
internal class MovieServiceTest {
    @Mock
    private lateinit var movieRepository: MovieRepository

    @Mock
    private lateinit var genreRepository: GenreRepository

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var directorRepository: DirectorRepository

    @Mock
    private lateinit var tmdbService: TmdbService

    @InjectMocks
    private lateinit var movieService: MovieService

    private lateinit var testMovie: Movie

    @BeforeEach
    fun setUp() {
        testMovie = Movie(
            "testTitle", "",
            LocalDate.of(2022, 1, 1), "Released",
            "movie.png", 100, "", ""
        )
        testMovie.id = 123L
        testMovie.averageRating = 1.23
        testMovie.ratingCount = 12
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 기본")
    fun testgetMovies1() {
        val keyword = ""
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        given(
            movieRepository.findByTitleLike(
                keyword,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"))
            )
        )
            .willReturn(
                PageImpl(
                    listOf(testMovie),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                    1
                )
            )

        val result = movieService.getMovies(keyword, page, pageSize, sortBy)

        AssertionsForClassTypes.assertThat(result).isNotNull()
        AssertionsForClassTypes.assertThat(result.items.size).isGreaterThan(0)
        // 반환된 PageDto 검증
        Assertions.assertEquals("$sortBy: ASC", result.sortBy)
        Assertions.assertEquals(pageSize, result.pageSize)
        Assertions.assertEquals(page, result.curPageNo)
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 검색")
    fun testgetMovies2() {
        val keyword = "해리"
        val sortBy = "id"
        val page = 1
        val pageSize = 10

        given(
            movieRepository.findByTitleLike(
                keyword,
                PageRequest.of(
                    page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")
                )
            )
        )
            .willReturn(
                PageImpl(
                    listOf(testMovie),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")),
                    1
                )
            )

        val result = movieService.getMovies(keyword, page, pageSize, sortBy)

        AssertionsForClassTypes.assertThat(result).isNotNull()
        AssertionsForClassTypes.assertThat(result.items.size).isGreaterThan(0)
        Assertions.assertEquals("$sortBy: ASC", result.sortBy)
        Assertions.assertEquals(pageSize, result.pageSize)
        Assertions.assertEquals(page, result.curPageNo)
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 평점 순 정렬")
    fun testgetMovies3() {
        val keyword = ""
        val sortBy = "rating"
        val page = 1
        val pageSize = 10

        given(
            movieRepository.findByTitleLike(
                keyword,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "averageRating"))
            )
        )
            .willReturn(
                PageImpl(
                    listOf(testMovie),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "averageRating")),
                    1
                )
            )

        val result = movieService.getMovies(keyword, page, pageSize, sortBy)

        AssertionsForClassTypes.assertThat(result).isNotNull()
        AssertionsForClassTypes.assertThat(result.items.size).isGreaterThan(0)
        Assertions.assertEquals("averageRating: DESC", result.sortBy)
        Assertions.assertEquals(pageSize, result.pageSize)
        Assertions.assertEquals(page, result.curPageNo)
    }

    @Test
    @DisplayName("영화 목록 조회 - 성공 - 리뷰수 순 정렬")
    fun testgetMovies4() {
        val keyword = ""
        val sortBy = "ratingCount"
        val page = 1
        val pageSize = 10

        given(
            movieRepository.findByTitleLike(
                keyword,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "ratingCount"))
            )
        )
            .willReturn(
                PageImpl(
                    listOf(testMovie),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "ratingCount")),
                    1
                )
            )

        val result = movieService.getMovies(keyword, page, pageSize, sortBy)

        AssertionsForClassTypes.assertThat(result).isNotNull()
        AssertionsForClassTypes.assertThat(result.items.size).isGreaterThan(0)
        Assertions.assertEquals("$sortBy: DESC", result.sortBy)
        Assertions.assertEquals(pageSize, result.pageSize)
        Assertions.assertEquals(page, result.curPageNo)
    }

    @Test
    @DisplayName("영화 목록 조회 - 실패 - 잘못된 정렬 기준")
    fun testgetMovies5() {
        val keyword = ""
        val sortBy = "unknown"
        val page = 1
        val pageSize = 10

        val thrown = AssertionsForClassTypes.catchThrowable {
            movieService.getMovies(
                keyword,
                page,
                pageSize,
                sortBy
            )
        }

        AssertionsForClassTypes.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("잘못된 정렬 기준입니다.")
    }

    @Test
    @DisplayName("영화 상세 조회 - 성공 - 기본")
    fun testgetMovie1() {
        given(movieRepository.findByIdWithCastsAndDirector(testMovie.id))
            .willReturn(testMovie)

        val result = movieService.getMovie(testMovie.id)

        AssertionsForClassTypes.assertThat(result).isNotNull()
        Assertions.assertEquals(testMovie.id, result.id)
        Assertions.assertEquals(testMovie.averageRating, result.averageRating)
        Assertions.assertEquals(testMovie.ratingCount, result.ratingCount)
        then(movieRepository).should().findByIdWithCastsAndDirector(testMovie.id)
    }

    @Test
    @DisplayName("영화 상세 조회 - 실패 - 없는 영화 조회")
    fun testgetMovie3() {
        val id = 1000000000000000000L
        given(movieRepository.findByIdWithCastsAndDirector(id)).willReturn(null)

        val thrown = AssertionsForClassTypes.catchThrowable { movieService.getMovie(id) }

        AssertionsForClassTypes.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${id}번 영화를 찾을 수 없습니다.")
    }

    @Test
    @DisplayName("영화 생성 - 성공")
    fun createMovie1() {
        // given
        val request = MovieRequest(
            "movie",
            "overview",
            LocalDate.of(2022, 1, 1),
            "Released",
            "posterPath",
            100,
            "productionCountry",
            "productionCompany",
            listOf(1L, 2L),
            listOf(MovieCastRequest(1L, "characterName")),
            1L
        )

        val genre1 = Genre("Action")
        val genre2 = Genre("Drama")
        val actor = Actor("Test Actor", null)
        val director = Director("Test Director", "PosterPath")

        val savedMovie = Movie(
            "movie",
            "overview",
            LocalDate.of(2022, 1, 1),
            "Released",
            "posterPath",
            100,
            "productionCountry",
            "productionCompany"
        )
        savedMovie.id = 1L
        savedMovie.genres.addAll(listOf(genre1, genre2))
        savedMovie.casts.add(MovieCast(savedMovie, actor, "characterName"))
        savedMovie.director = director

        // when
        whenever(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        whenever(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        whenever(directorRepository.findById(1L)).thenReturn(Optional.of(director))
        whenever(movieRepository.save(any<Movie>())).thenReturn(savedMovie)

        val response = movieService.createMovie(request)

        // then
        verify(movieRepository).save(any<Movie>())
        Assertions.assertNotNull(response)
        Assertions.assertEquals(savedMovie.id, response.id)
        Assertions.assertEquals(savedMovie.title, response.title)
        Assertions.assertEquals("Action", response.genres[0].name)
        Assertions.assertEquals("Drama", response.genres[1].name)
        Assertions.assertEquals("Test Actor", response.casts[0].actor.name)
        Assertions.assertEquals("characterName", response.casts[0].characterName)
        Assertions.assertEquals("Test Director", response.director?.name)
    }

    @Test
    @DisplayName("영화 수정 - 성공")
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

        val genre1 = Genre("Action")
        val genre2 = Genre("Drama")
        val actor = Actor("Actor Name", null)
        val director = Director("Director Name", "posterPath")

        val movie = Movie(
            "old title", "old overview", LocalDate.of(2020, 1, 1),
            "old status", "old.png", 90, "Korea", "Old Company"
        )
        movie.id = movieId

        // mocking
        whenever(movieRepository.findById(movieId)).thenReturn(Optional.of(movie))
        whenever(genreRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(genre1, genre2))
        whenever(actorRepository.findById(1L)).thenReturn(Optional.of(actor))
        whenever(directorRepository.findById(1L)).thenReturn(Optional.of(director))

        // when
        val response = movieService.updateMovie(movieId, request)

        // then
        Assertions.assertEquals("updated title", response.title)
        Assertions.assertEquals("updated overview", response.overview)
        Assertions.assertEquals("updated.png", response.posterPath)
        Assertions.assertEquals("Released", response.status)
        Assertions.assertEquals(120, response.runtime)
        Assertions.assertEquals("USA", response.productionCountry)
        Assertions.assertEquals("Updated Company", response.productionCompany)
        Assertions.assertEquals(2, response.genres.size)
        Assertions.assertEquals("Action", response.genres[0].name)
        Assertions.assertEquals("Drama", response.genres[1].name)
        Assertions.assertEquals("Actor Name", response.casts[0].actor.name)
        Assertions.assertEquals("new role", response.casts[0].characterName)
        Assertions.assertEquals("Director Name", response.director?.name)
    }

    @Test
    @DisplayName("영화 수정 - 실패 - 없는 영화")
    fun updateMovie2() {
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

        // mocking
        whenever(movieRepository.findById(movieId)).thenReturn(Optional.empty())

        // when
        val thrown = AssertionsForClassTypes.catchThrowable {
            movieService.updateMovie(
                movieId,
                request
            )
        }

        // then
        AssertionsForClassTypes.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${movieId}번 영화를 찾을 수 없습니다.")
    }

    @Test
    @DisplayName("영화 삭제 - 성공")
    fun deleteMovie1() {
        // given
        val movieId = 1L
        val movie = Movie(
            "movie",
            "overview",
            LocalDate.of(2022, 1, 1),
            "Released",
            "posterPath",
            100,
            "productionCountry",
            "productionCompany"
        )

        whenever(movieRepository.findById(movieId))
            .thenReturn(Optional.of(movie))

        // when
        movieService.deleteMovie(movieId)

        // then
        verify(movieRepository).delete(movie)
    }

    @Test
    @DisplayName("영화 삭제 - 실패 - 없는 영화")
    fun deleteMovie2() {
        // given
        val movieId = 1L
        whenever(movieRepository.findById(movieId))
            .thenReturn(Optional.empty())

        // when
        val thrown = AssertionsForClassTypes.catchThrowable { movieService.deleteMovie(movieId) }

        // then
        AssertionsForClassTypes.assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
            .hasMessage("${movieId}번 영화를 찾을 수 없습니다.")
    }
}