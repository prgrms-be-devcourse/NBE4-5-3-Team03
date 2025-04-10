package com.example.Flicktionary.domain.user.controller

import com.example.Flicktionary.domain.user.dto.UserAccountDto
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.security.CustomUserDetailsService
import com.example.Flicktionary.global.utils.JwtUtils
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@DisplayName("회원 도메인 컨트롤러 테스트")
// 퍼포먼스를 위해 필요한 모듈만 초기화
@Import(UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class)
@WebMvcTest(UserAccountController::class)
// 테스트 실행 시 Spring Security 비활성화
@AutoConfigureMockMvc(addFilters = false)
class UserAccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    @DisplayName("회원 가입을 한다.")
    @Test
    fun givenValidDto_whenRegisteringUser_thenRegistersUser() {
        given(userAccountService.registerUser(any<UserAccountDto>()))
            .willReturn(UserAccountDto(
                1L,
                "testUsername",
                "testPassword",
                "test@email.com",
                "testNickname",
                "USER"
        ))

        mockMvc.perform(
            post("/api/users/register")
                .content("{\"username\": \"testUsername\"," +
                        "\"password\": \"testPassword\"," +
                        "\"email\": \"test@email.com\"," +
                        "\"nickname\": \"testNickname\"," +
                        "\"role\": \"USER\"}")
                .contentType("application/json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("1"))
            .andExpect(jsonPath("$.data.username").value("testUsername"))
            .andExpect(jsonPath("$.data.password").value("testPassword"))
            .andExpect(jsonPath("$.data.email").value("test@email.com"))
            .andExpect(jsonPath("$.data.nickname").value("testNickname"))
            .andExpect(jsonPath("$.data.role").value("USER"))

        then(userAccountService).should().registerUser(any<UserAccountDto>())
    }

    @DisplayName("로그인 요청을 받으면 새 접근 토큰과 리프레시 토큰을 쿠키로 발행한다.")
    @Test
    fun givenUserCredentials_whenLoggingIn_thenReturnsNewTokens() {
        val username = "testUsername"
        val password = "testPassword"
        given(userAccountJwtAuthenticationService
            .createNewAccessTokenForUser(username, password))
            .willReturn("fakeAccessToken")
        given(userAccountJwtAuthenticationService
            .rotateRefreshTokenOfUser(username))
            .willReturn("fakeRefreshToken")

        mockMvc.perform(
            post("/api/users/login")
                .content("{\"username\": \"testUsername\", \"password\": \"testPassword\"}")
                .contentType("application/json")
                .characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            // TODO: 매번 쿠키 설정을 assert하는 대신 전용 테스트 케이스로 분리. 관련 메소드가 private이기 때문에 UserAccountController.logoutUser 메소드를 사용하면 될 듯.
            .andExpect(cookie().value("accessToken", "fakeAccessToken"))
            .andExpect(cookie().httpOnly("accessToken", true))
            .andExpect(cookie().maxAge("accessToken", 25920000))
            .andExpect(cookie().value("refreshToken", "fakeRefreshToken"))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().maxAge("refreshToken", 25920000))
            .andExpect(jsonPath("$.message").value("토큰이 성공적으로 발행되었습니다."))

        then(userAccountJwtAuthenticationService).should()
            .createNewAccessTokenForUser(username, password)
        then(userAccountJwtAuthenticationService).should()
            .rotateRefreshTokenOfUser(username)
    }

    @DisplayName("로그아웃 요청을 받으면 토큰 쿠키를 비운다.")
    @Test
    fun givenNothing_whenLoggingOut_thenReturnsEmptyTokens() {
        mockMvc.perform(
            get("/api/users/logout")
                .cookie(Cookie("refreshToken", "faketoken")))
            .andExpect(status().isOk())
            .andExpect(cookie().value("accessToken", ""))
            .andExpect(cookie().httpOnly("accessToken", true))
            .andExpect(cookie().maxAge("accessToken", 25920000))
            .andExpect(cookie().value("refreshToken", ""))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().maxAge("refreshToken", 25920000))
            .andExpect(jsonPath("$.message").value("쿠키가 성공적으로 비워졌습니다."))
    }

    @DisplayName("리프레시 토큰으로 접근 토큰을 쿠키로 재발행한다.")
    @Test
    fun givenRefreshToken_whenRequestingFreshAccessToken_thenReturnsNewTokens() {
        given(userAccountJwtAuthenticationService
            .createNewAccessTokenWithRefreshToken("faketoken"))
            .willReturn(JwtUtils.TokenSet("fakeAccessToken", "fakeRefreshToken"))

        mockMvc.perform(
            get("/api/users/refresh")
                .cookie(Cookie("refreshToken", "faketoken")))
            .andExpect(status().isOk())
            .andExpect(cookie().value("accessToken", "fakeAccessToken"))
            .andExpect(cookie().httpOnly("accessToken", true))
            .andExpect(cookie().maxAge("accessToken", 25920000))
            .andExpect(cookie().value("refreshToken", "fakeRefreshToken"))
            .andExpect(cookie().httpOnly("refreshToken", true))
            .andExpect(cookie().maxAge("refreshToken", 25920000))
            .andExpect(jsonPath("$.message").value("토큰이 성공적으로 재발행되었습니다."))

        then(userAccountJwtAuthenticationService).should()
            .createNewAccessTokenWithRefreshToken(any<String>())
    }

    @DisplayName("인증 정보가 있는 클라이언트에게 로그인 상태 확인 요청을 받으면, 인증 정보가 있다고 응답한다.")
    @Test
    fun givenCredentials_whenRequestingCredentialStatus_thenReturnOk() {
        mockMvc.perform(
            get("/api/users/status")
                .cookie(
                    Cookie("accessToken", "faketoken"),
                    Cookie("refreshToken", "faketoken")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("인증 정보가 존재합니다."))
    }

    @DisplayName("인증 정보가 없는 클라이언트에게 로그인 상태 확인 요청을 받으면, 인증 정보가 없다고 응답한다.")
    @Test
    fun givenNoCredentials_whenRequestingCredentialStatus_thenReturnForbidden() {
        mockMvc.perform(
            get("/api/users/status"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("인증 정보가 존재하지 않습니다."))
    }
}