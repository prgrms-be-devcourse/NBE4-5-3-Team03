package com.example.Flicktionary.domain.genre.service

import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class GenreServiceTest {

    @Mock
    private lateinit var genreRepository: GenreRepository

    @InjectMocks
    private lateinit var genreService: GenreService

    @Test
    @DisplayName("장르 검색 - 성공")
    fun getGenres() {
        // given
        val keyword = "Action"
        val formattedKeyword = "action"
        val mockGenres = listOf(
            Genre(name = "Action"),
            Genre(name = "Action Movie")
        )

        `when`(genreRepository.findByNameLike(formattedKeyword)).thenReturn(mockGenres)

        // when
        val result = genreService.getGenres(keyword)

        // then
        assertEquals(mockGenres, result)
    }
}