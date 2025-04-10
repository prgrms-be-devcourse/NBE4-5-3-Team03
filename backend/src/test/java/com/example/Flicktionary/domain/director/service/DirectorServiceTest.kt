package com.example.Flicktionary.domain.director.service

import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class DirectorServiceTest {
    @Mock
    private lateinit var directorRepository: DirectorRepository

    @Mock
    private lateinit var movieRepository: MovieRepository

    @Mock
    private lateinit var seriesRepository: SeriesRepository

    @InjectMocks
    private lateinit var directorService: DirectorService

    private lateinit var director: Director
    private lateinit var movie: Movie
    private lateinit var series: Series

    @BeforeEach
    fun setUp() {
        director = Director("director", "director.png")

        movie = Movie(
            "movie", "",
            LocalDate.of(2022, 1, 1), "",
            "movie.png", 100, "", ""
        ).apply {
            id = 1L
            this.director = this@DirectorServiceTest.director
        }

        series = Series(
            "series", "",
            LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
            "", "series.png", 10, "", ""
        ).apply {
            id = 1L
            this.director = this@DirectorServiceTest.director
        }
    }

    @DisplayName("감독 목록 조회 - 성공")
    @Test
    fun getDirectors() {
        // Given
        val keyword = ""
        val page = 1
        val pageSize = 10
        val pageable: Pageable = PageRequest.of(page - 1, pageSize)
        val directorPage = PageImpl(listOf(director), pageable, 1)

        given(
            directorRepository.findByNameLike(
                any(), eq(pageable)
            )
        ).willReturn(directorPage)

        // When
        val result = directorService.getDirectors(keyword, page, pageSize)

        // Then
        assertThat(result).isNotNull().isNotEmpty()
        assertEquals(1, result.content.size)
        assertEquals("director", result.content[0].name)

        then(directorRepository).should().findByNameLike(
            any(), eq(pageable)
        )
    }

    @Test
    @DisplayName("감독 상세 조회 - 성공")
    fun getDirector() {
        // Given
        given(directorRepository.findById(1L))
            .willReturn(Optional.of(director))

        // When
        val result = directorService.getDirector(1L)

        // Then
        assertThat(result).isNotNull()
        assertEquals(director, result)

        then(directorRepository).should().findById(1L)
    }

    @DisplayName("감독 아이디로 영화 조회 - 성공")
    @Test
    fun getMoviesByDirectorId() {
        // Arrange
        whenever(movieRepository.findByDirectorId(director.id))
            .thenReturn(listOf(movie))

        // Act
        val movies = directorService.getMoviesByDirectorId(director.id)

        // Assert
        assertNotNull(movies)
        assertEquals(1, movies.size)
        assertEquals(movie.title, movies[0].title)
    }

    @DisplayName("감독 아이디로 시리즈 조회 - 성공")
    @Test
    fun getSeriesByDirectorId() {
        // Arrange
        whenever(seriesRepository.findByDirectorId(director.id))
            .thenReturn(listOf(series))

        // Act
        val seriesList = directorService.getSeriesByDirectorId(director.id)

        // Assert
        assertNotNull(seriesList)
        assertEquals(1, seriesList.size)
        assertEquals(series.title, seriesList[0].title)
    }
}