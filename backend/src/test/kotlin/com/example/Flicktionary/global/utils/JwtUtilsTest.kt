package com.example.Flicktionary.global.utils

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.BDDAssertions.assertThat
import org.assertj.core.api.BDDAssertions.catchThrowable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("JWT 유틸리티 클래스 테스트")
class JwtUtilsTest {

    private val testSecretKey = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890"

    private val testExpireInSeconds = 60

    @DisplayName("JWT의 클레임을 정상적으로 꺼내올 수 있어야 한다.")
    @Test
    fun areClaimsCorrectlyRetrievedFromJwt() {
        val token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null)
        val claims = JwtUtils.getTokenPayload(testSecretKey, token)
        assertNotNull(claims)
        assertTrue(claims.containsKey("iat"))
        assertTrue(claims.containsKey("exp"))
    }

    @DisplayName("클레임이 주어졌을때 그 클레임이 모두 포함된 JWT가 생성된다.")
    @Test
    fun isTokenCreatedWithAllGivenClaims() {
        val claims = mapOf(
            "testClaim" to "testClaimValue",
            "anotherTestClaim" to "anotherTestClaimValue"
        )
        val token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, claims)
        val retrievedClaims = JwtUtils.getTokenPayload(testSecretKey, token)
        assertTrue(retrievedClaims.containsKey("testClaim"))
        assertTrue(retrievedClaims.containsKey("anotherTestClaim"))
        // 발행/만료시각 클레임
        assertTrue(retrievedClaims.containsKey("iat"))
        assertTrue(retrievedClaims.containsKey("exp"))
        assertEquals(retrievedClaims["testClaim"], claims["testClaim"])
        assertEquals(retrievedClaims["anotherTestClaim"], claims["anotherTestClaim"])
    }

    @DisplayName("JWT의 유효기간이 주어졌을때 만료시각이 정확하게 설정된다.")
    @Test
    fun isTokenCreatedWithCorrectExpiryDate() {
        val token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null)
        val claims = JwtUtils.getTokenPayload(testSecretKey, token)
        assertEquals(testExpireInSeconds.toLong(), claims["exp"] as Long - claims["iat"] as Long)
    }

    @DisplayName("exp 클레임이 없는 JWT는 유효하지 않아야 한다.")
    @Test
    fun isTokenWithoutExpClaimCorrectlyIdentified() {
        val hmacSecretKey = Keys.hmacShaKeyFor(testSecretKey.toByteArray())
        val token = Jwts.builder()
            .issuedAt(Date())
            .signWith(hmacSecretKey)
            .compact()
        val thrown = catchThrowable { JwtUtils.isTokenValid(testSecretKey, token) }
        assertThat(thrown)
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("토큰의 만료시각이 없습니다.")
    }

    @DisplayName("유효한 JWT는 검증을 통과해야 한다.")
    @Test
    fun isValidTokenCorrectlyIdentified() {
        val token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null)
        assertTrue(JwtUtils.isTokenValid(testSecretKey, token))
    }

    @DisplayName("만료시각이 지난 JWT는 유효하지 않아야 한다.")
    @Test
    fun isExpiredTokenCorrectlyIdentified() {
        val expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMiwiZXhwIjoxNTE2MjM5MDJ9.L0RzBArnjkDZIVMq_NHbmpJWMfuaTUdFCkV0FgKV9Lc"
        val thrown = catchThrowable { JwtUtils.isTokenValid(testSecretKey, expiredToken) }
        assertThat(thrown)
            .isInstanceOf(ExpiredJwtException::class.java)
    }
}