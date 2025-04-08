package com.example.Flicktionary.global.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

/**
 * JWT 관련 유틸리티 메소드가 담긴 클래스.
 */
object JwtUtils {
    /**
     * 비밀키로 서명된, 만료시각을 가진 JWT를 생성한다.
     * @param secret JWT를 서명할 비밀키
     * @param expireAfterSeconds JWT의 유효기간(초)
     * @param claims 생성될 JWT에 추가로 담길 클레임
     * @return 새로 생성된 JWT
     */
    @JvmStatic
    fun createToken(secret: String, expireAfterSeconds: Int, claims: Map<String, Any>?): String {
        val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
        val issuedAt = Date()
        val expiresAt = Date(issuedAt.time + expireAfterSeconds * 1000L)
        return Jwts.builder()
            .claims(claims)
            .issuedAt(issuedAt)
            .expiration(expiresAt)
            .signWith(secretKey)
            .compact()
    }

    /**
     * JWT의 유효성을 검증한다.
     * @param secret JWT의 서명을 검증할 비밀키
     * @param token 검증할 JWT
     * @return 유효할시 {@code true}
     */
    @JvmStatic
    fun isTokenValid(secret: String, token: String): Boolean {
        try {
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(token)
                .payload as Map<String, Any>
            if (!claims.containsKey("exp")) {
                throw RuntimeException("토큰의 만료시각이 없습니다.")
            }
            // TODO: 만료된 토큰을 검증할때 JJWT 라이브러리가 만료시각까지 확인하는 것 같은데, 거기에 맞춰 검증 로직 수정
            val exp = Date(claims["exp"] as Long * 1000L)
            if (exp.before(Date())) {
                throw RuntimeException("토큰이 만료되었습니다.")
            }
        } catch (e: Exception) {
            // TODO: 토큰이 유효하지 않을때 예외를 던질지, false를 반환할지 결정
            throw e
        }
        return true
    }

    /**
     * JWT를 검증한 뒤 클레임을 {@code Map}으로 꺼내옵니다.
     * @param secret JWT의 서명을 검증할 비밀키
     * @param token 검증할 JWT
     * @return JWT의 클레임이 담긴 {@code Map}
     */
    @JvmStatic
    fun getTokenPayload(secret: String, token: String): Map<String, Any> {
        val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parse(token)
            .payload as Map<String, Any>
    }

    /**
     * 접근 토큰과 리프레시 토큰을 담는 레코드.
     * @param access 접근 토큰
     * @param refresh 리프레시 토큰
     */
    public data class TokenSet (
        val access: String,
        val refresh: String
    ) {}
}