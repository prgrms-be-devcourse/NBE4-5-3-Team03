package com.example.Flicktionary.global.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT 유틸리티 클래스 테스트")
public class JwtUtilsTest {

    private final String testSecretKey = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";

    private final Integer testExpireInSeconds = 60;

    @DisplayName("JWT의 클레임을 정상적으로 꺼내올 수 있어야 한다.")
    @Test
    void areClaimsCorrectlyRetrievedFromJwt() {
        String token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null);
        Map<String, Object> claims = JwtUtils.getTokenPayload(testSecretKey, token);
        assertNotNull(claims);
        assertTrue(claims.containsKey("iat"));
        assertTrue(claims.containsKey("exp"));
    }

    @DisplayName("클레임이 주어졌을때 그 클레임이 모두 포함된 JWT가 생성된다.")
    @Test
    void isTokenCreatedWithAllGivenClaims() {
        Map<String, Object> claims = Map.of(
                "testClaim", "testClaimValue",
                "anotherTestClaim", "anotherTestClaimValue"
        );
        String token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, claims);
        Map<String, Object> retrievedClaims = JwtUtils.getTokenPayload(testSecretKey, token);
        assertTrue(retrievedClaims.containsKey("testClaim"));
        assertTrue(retrievedClaims.containsKey("anotherTestClaim"));
        // 발행/만료시각 클레임
        assertTrue(retrievedClaims.containsKey("iat"));
        assertTrue(retrievedClaims.containsKey("exp"));
        assertEquals(retrievedClaims.get("testClaim"), claims.get("testClaim"));
        assertEquals(retrievedClaims.get("anotherTestClaim"), claims.get("anotherTestClaim"));
    }

    @DisplayName("JWT의 유효기간이 주어졌을때 만료시각이 정확하게 설정된다.")
    @Test
    void isTokenCreatedWithCorrectExpiryDate() {
        String token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null);
        Map<String, Object> claims = JwtUtils.getTokenPayload(testSecretKey, token);
        assertEquals(testExpireInSeconds.longValue(), (Long) claims.get("exp") - (Long) claims.get("iat"));
    }

    @DisplayName("exp 클레임이 없는 JWT는 유효하지 않아야 한다.")
    @Test
    void isTokenWithoutExpClaimCorrectlyIdentified() {
        SecretKey hmacSecretKey = Keys.hmacShaKeyFor(testSecretKey.getBytes());
        String token = Jwts.builder()
                .issuedAt(new Date())
                .signWith(hmacSecretKey)
                .compact();
        Throwable thrown = catchThrowable(() -> JwtUtils.isTokenValid(testSecretKey, token));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("토큰의 만료시각이 없습니다.");
    }

    @DisplayName("유효한 JWT는 검증을 통과해야 한다.")
    @Test
    void isValidTokenCorrectlyIdentified() {
        String token = JwtUtils.createToken(testSecretKey, testExpireInSeconds, null);
        assertTrue(JwtUtils.isTokenValid(testSecretKey, token));
    }

    @DisplayName("만료시각이 지난 JWT는 유효하지 않아야 한다.")
    @Test
    void isExpiredTokenCorrectlyIdentified() {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMiwiZXhwIjoxNTE2MjM5MDJ9.L0RzBArnjkDZIVMq_NHbmpJWMfuaTUdFCkV0FgKV9Lc";
        Throwable thrown = catchThrowable(() -> JwtUtils.isTokenValid(testSecretKey, expiredToken));
        assertThat(thrown)
                .isInstanceOf(ExpiredJwtException.class);
    }
}
