package com.example.Flicktionary.domain.favorite.controller

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto
import com.example.Flicktionary.domain.favorite.entity.ContentType
import com.example.Flicktionary.domain.favorite.service.FavoriteService
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.security.CustomUserDetailsService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals


@DisplayName("즐겨찾기 도메인 컨트롤러 테스트")
@Import(
    FavoriteService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(FavoriteController::class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var favoriteService: FavoriteService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    private lateinit var favoriteDto: FavoriteDto

//    private fun <T> anyOfType(clazz: Class<T>): T {
//        Mockito.any<T>(clazz)
//        return uninitialized()
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    private fun <T> uninitialized(): T = null as T

    @Test
    @DisplayName("즐겨찾기 추가 - 성공")
    fun createFavorite() {
        // Given
        val userId = 1L
        val contentType = ContentType.MOVIE
        val contentId = 599L
        val captor = argumentCaptor<FavoriteDto>()

        given(favoriteService.createFavorite(captor.capture()))
            .willReturn(
                FavoriteDto(
                    id = 0L,
                    userId = userId,
                    contentType = contentType,
                    contentId = contentId
                )
            )

        // When & Then
        val resultActions = mvc
            .perform(
                post("/api/favorites")
                    .content(
                        """
                                        {
                                            "userId": "${userId}",
                                            "contentType": "${contentType}",
                                            "contentId": "${contentId}"
                                        }
                                        
                                        """
                            .trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isCreated())
            .andExpect(handler().handlerType(FavoriteController::class.java))
            .andExpect(handler().methodName("createFavorite"))
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andExpect(jsonPath("$.data.contentType").value(contentType.toString()))
            .andExpect(jsonPath("$.data.contentId").value(contentId))

        val captured = captor.firstValue

        assertEquals(userId, captured.userId)
        assertEquals(contentType, captured.contentType)
        assertEquals(contentId, captured.contentId)

        then(favoriteService).should().createFavorite(any())
    }

    @Test
    @DisplayName("즐겨찾기 조회 - 성공")
    fun getFavorite() {
        // Given
        val userId = 1L
        val longCaptor = argumentCaptor<Long>()
        val integerCaptor = argumentCaptor<Int>()
        val stringCaptor = argumentCaptor<String>()

        given(
            favoriteService.getUserFavorites(
                longCaptor.capture(),
                integerCaptor.capture(),
                integerCaptor.capture(),
                stringCaptor.capture(),
                stringCaptor.capture()
            )
        ).willReturn(
            PageDto(
                PageImpl(
                    listOf(), PageRequest.of(0, 10), 10
                )
            )
        )

        // When & Then
        val resultActions = mvc
            .perform(
                get("/api/favorites/${userId}")
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(print())

        resultActions.andExpect(status().isOk())

        val capturedLong = longCaptor.firstValue
        val capturedIntegers = integerCaptor.allValues
        val capturedStrings = stringCaptor.allValues

        assertEquals(userId, capturedLong)
        assertEquals(1, capturedIntegers[0]) // page
        assertEquals(10, capturedIntegers[1]) // pageSize
        assertEquals("id", capturedStrings[0]) // sortBy
        assertEquals("desc", capturedStrings[1]) // direction

        then(favoriteService).should().getUserFavorites(userId, 1, 10, "id", "desc")
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 성공")
    fun deleteFavorite() {
        // Given
        val id = 1L
        val captor = argumentCaptor<Long>()
        doNothing().`when`(favoriteService).deleteFavorite(captor.capture())

        // When & Then
        val resultActions = mvc
            .perform(
                delete("/api/favorites/${id}")
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isNoContent())
            .andExpect(handler().handlerType(FavoriteController::class.java))
            .andExpect(handler().methodName("deleteFavorite"))
        then(favoriteService).should().deleteFavorite(id)
    }
}
