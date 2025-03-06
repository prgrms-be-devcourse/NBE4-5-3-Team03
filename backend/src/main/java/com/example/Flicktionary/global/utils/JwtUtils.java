package com.example.Flicktionary.global.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 관련 유틸리티 메소드가 담긴 클래스.
 */
public class JwtUtils {
    /**
     * 비밀키로 서명된, 만료시각을 가진 JWT를 생성한다.
     * @param secret JWT를 서명할 비밀키
     * @param expireAfterSeconds JWT의 유효기간(초)
     * @param claims 생성될 JWT에 추가로 담길 클레임
     * @return 새로 생성된 JWT
     */
    public static String createToken(String secret, Integer expireAfterSeconds, Map<String, Object> claims) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expireAfterSeconds * 1000L);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT의 유효성을 검증한다.
     * @param secret JWT의 서명을 검증할 비밀키
     * @param token 검증할 JWT
     * @return 유효할시 {@code true}
     */
    public static boolean isTokenValid(String secret, String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            Map<String, Object> claims = (Map<String, Object>) Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token)
                    .getPayload();
            if (!claims.containsKey("exp")) {
                throw new RuntimeException("토큰의 만료시각이 없습니다.");
            }
            Date exp = new Date((Long) claims.get("exp") * 1000L);
            if (exp.before(new Date())) {
                throw new RuntimeException("토큰이 만료되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * JWT를 검증한 뒤 클레임을 {@code Map}으로 꺼내옵니다.
     * @param secret JWT의 서명을 검증할 비밀키
     * @param token 검증할 JWT
     * @return JWT의 클레임이 담긴 {@code Map}
     */
    public static Map<String, Object> getTokenPayload(String secret, String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        return (Map<String, Object>) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(token)
                .getPayload();
    }

    /**
     * 접근 토큰과 리프레시 토큰을 담는 레코드.
     * @param access 접근 토큰
     * @param refresh 리프레시 토큰
     */
    public record TokenSet (
            String access,
            String refresh
    ) {}
}
