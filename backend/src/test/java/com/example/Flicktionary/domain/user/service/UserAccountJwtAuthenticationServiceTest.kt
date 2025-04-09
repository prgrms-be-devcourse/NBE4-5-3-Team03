package com.example.Flicktionary.domain.user.service

import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import com.example.Flicktionary.global.utils.JwtUtils
import org.assertj.core.api.BDDAssertions.assertThat
import org.assertj.core.api.BDDAssertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("회원 JWT 인증 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class UserAccountJwtAuthenticationServiceTest {

    private var userAccount = UserAccount(
        1L,
        "testUserAccount",
        "testUserAccountPassword",
        "test@email.com",
        "testNickname",
        UserAccountType.ADMIN
    )

    private val testSecret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890"

    private val testAccessExpireSeconds = 60

    private val testRefreshExpireDays = 3

    @Mock
    private lateinit var userAccountRepository: UserAccountRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    @BeforeEach
    fun injectAutowiredValues() {
        ReflectionTestUtils.setField(
            userAccountJwtAuthenticationService,
            "secret",
            testSecret)
        ReflectionTestUtils.setField(
            userAccountJwtAuthenticationService,
            "accessExpireSeconds",
            testAccessExpireSeconds)
        ReflectionTestUtils.setField(
            userAccountJwtAuthenticationService,
            "refreshExpireDays",
            testRefreshExpireDays)
    }
    
    @DisplayName("올바른 인증 정보가 주어졌을때 해당 유저에게 접근 토큰을 발행한다.")
    @Test
    fun givenCorrectUserCredentialsWhenCreatingTokenThenReturnTokenWithCorrectClaims() {
        given(userAccountRepository.findByUsername(anyString())).willReturn(userAccount)
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true)

        val token = userAccountJwtAuthenticationService.createNewAccessTokenForUser(
                userAccount.username,
        userAccount.password)
        val claims = JwtUtils.getTokenPayload(testSecret, token)

        assertTrue(claims.containsKey("username"))
        assertTrue(claims.containsKey("nickname"))
        assertEquals(userAccount.username, claims["username"])
        assertEquals(userAccount.nickname, claims["nickname"])
        then(userAccountRepository).should().findByUsername(userAccount.username)
    }

    @DisplayName("올바르지 않은 인증 정보가 주어졌을때 예외를 던진다.")
    @Test
    fun givenIncorrectUserCredentialsWhenCreatingTokenThenThrowException() {
        given(userAccountRepository.findByUsername(userAccount.username)).willReturn(userAccount)

        val thrown = catchThrowable {
            userAccountJwtAuthenticationService.createNewAccessTokenForUser(
                userAccount.username,
                "wrongPassword"
            )
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessage("비밀번호가 틀립니다.")
    }

    @DisplayName("올바른 리프레시 토큰이 주어졌을때 접근 토큰와 리프레시 토큰을 재발행한다.")
    @Test
    fun givenValidRefreshTokenWhenRefreshingTokensThenReturnRefreshedTokens() {
        given(userAccountRepository.findByRefreshToken(anyString())).willReturn(userAccount)
        given(userAccountRepository.findByUsername(anyString())).willReturn(userAccount)

        val fakeRefreshToken = "=#=#=#=#=#=#=#=#=#=#=#=#"
        val fakeExpiryDate = LocalDateTime.of(3000, 1, 1, 0, 0)
        userAccount.refreshToken = fakeRefreshToken
        userAccount.refreshTokenExpiresAt = fakeExpiryDate

        val tokens = userAccountJwtAuthenticationService
                .createNewAccessTokenWithRefreshToken(userAccount.refreshToken!!)

        assertNotEquals(fakeRefreshToken, userAccount.refreshToken)
        assertNotEquals(fakeExpiryDate, userAccount.refreshTokenExpiresAt)
        assertThat(tokens.access).isNotEmpty()
        then(userAccountRepository).should().findByRefreshToken(fakeRefreshToken)
        then(userAccountRepository).should().findByUsername(userAccount.username)
    }

    @DisplayName("만료된 리프레시 토큰이 주어졌을때 예외를 던진다.")
    @Test
    fun givenStaleRefreshTokenWhenRefreshingTokensThenThrowException() {
        given(userAccountRepository.findByRefreshToken(anyString())).willReturn(userAccount)

        val wrongRefreshToken = "=#=#=#=#=#=#=#=#=#=#=#=#"
        val fakeExpiryDate = LocalDateTime.of(3000, 1, 1, 0, 0)
        userAccount.refreshToken = "fakeRefreshToken"
        userAccount.refreshTokenExpiresAt = fakeExpiryDate

        val thrown = catchThrowable {
            userAccountJwtAuthenticationService
                .createNewAccessTokenWithRefreshToken(wrongRefreshToken)
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessage("리프레시 토큰이 유효하지 않습니다.")
        then(userAccountRepository).should().findByRefreshToken(wrongRefreshToken)
    }
}