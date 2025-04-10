package com.example.Flicktionary.domain.genre.controller

import com.example.Flicktionary.domain.genre.dto.GenreRequest
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.service.GenreService
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.security.CustomUserDetailsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@Import(
    GenreService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(GenreController::class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var genreService: GenreService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    @MockitoBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    private val objectMapper = ObjectMapper()

    @Test
    @DisplayName("영화 검색 - 성공")
    fun getGenres() {
        // given
        val keyword = "Action"
        val genres = listOf(Genre("Action"), Genre("Action Adventure"))

        // when
        whenever(genreService.getGenres(keyword)).thenReturn(genres)

        // then
        mvc.get("/api/genres") {
            param("keyword", keyword)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value("200") }
            jsonPath("$.message") { value("정상 처리되었습니다.") }
            jsonPath("$.data.size()") { value(2) }
            jsonPath("$.data[0].name") { value("Action") }

        }
    }

    @Test
    @DisplayName("영화 등록 - 성공")
    fun createGenre1() {
        // given
        val request = GenreRequest("Action")

        // when
        whenever(genreService.createGenre(request)).thenReturn(Genre("Action"))

        // then
        mvc.post("/api/genres") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.code") { value("201") }
            jsonPath("$.data.name") { value("Action") }
        }
    }
}