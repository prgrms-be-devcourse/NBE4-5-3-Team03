package com.example.Flicktionary.domain.user.service

import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import com.example.Flicktionary.global.utils.JwtUtils
import com.example.Flicktionary.global.utils.UuidUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * JWT를 통한 회원 인증을 지원하기 위한 서비스.
 */
@Service
@Transactional
class UserAccountJwtAuthenticationService(
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    /**
     * JWT 서명/검증에 사용될 비밀키
     */
    @Value("\${custom.jwt.secret-key}")
    private lateinit var secret: String

    /**
     * 접큰 토큰의 유효기간(초)
     */
    @Value("\${custom.jwt.access-expire-seconds}")
    private var accessExpireSeconds: Int = 0

    /**
     * 리프레시 토큰의 유효기간(일)
     */
    @Value("\${custom.jwt.refresh-expire-days}")
    private var refreshExpireDays: Int = 0

    /**
     * 인증 정보를 검증한 뒤, 해당 회원에게 접근 토큰을 발행한다.
     *
     * @param username 회원의 유저 ID
     * @param password 회원의 비밀번호
     * @return 새로 발행된 접근 토큰
     */
    fun createNewAccessTokenForUser(username: String, password: String): String {
        val userAccount = getUserByUsername(username)
        if (!passwordEncoder.matches("{bcrypt}${password}", userAccount.password)) {
            throw ServiceException(HttpStatus.FORBIDDEN.value(), "비밀번호가 틀립니다.")
        }
        return createNewAccessTokenWithClaims(userAccount.username, userAccount.nickname)
    }

    /**
     * 주어진 유저 ID와 닉네임이 클레임에 담긴 접근 토큰을 발행한다.
     *
     * @param username 회원의 유저 ID
     * @param nickname 회원의 닉네임
     * @return 새로 발행된 접근 토큰
     */
    private fun createNewAccessTokenWithClaims(username: String, nickname: String): String {
        val claims = mapOf(
            "username" to username,
            "nickname" to nickname
        )
        return JwtUtils.createToken(secret, accessExpireSeconds, claims)
    }

    

    /**
     * 주어진 리프레시 토큰을 검증한 뒤, 성공하면 새로운 접근 토큰과 리프레시 토큰을 발행하여 그것들을 반환한다.
     *
     * @param refreshTokenBase64 리프레시 토큰
     * @return 새로 발행된 접근 토큰과 리프레시 토큰이 담긴 {@link com.example.Flicktionary.global.utils.JwtUtils.TokenSet} 오브젝트
     */
    fun createNewAccessTokenWithRefreshToken(refreshTokenBase64: String): JwtUtils.TokenSet {
        val userAccount = getUserByRefreshToken(refreshTokenBase64)
        if (!isRefreshTokenFresh(userAccount) || !isRefreshTokenValid(userAccount, refreshTokenBase64)) {
            throw ServiceException(HttpStatus.FORBIDDEN.value(), "리프레시 토큰이 유효하지 않습니다.")
        }

        val newAccessToken = createNewAccessTokenWithClaims(userAccount.username, userAccount.nickname)
        val newRefreshToken = rotateRefreshTokenOfUser(userAccount.username)
        return JwtUtils.TokenSet(newAccessToken, newRefreshToken)
    }

    /**
     * 접큰 토큰의 클레임에서 회원 정보를 추출한 뒤, 해당하는 회원의 엔티티 오브젝트를 반환한다.
     *
     * @param token 접근 토큰
     * @return 접근 토큰이 발행된 회원의 엔티티 오브젝트
     */
    fun retrieveUserFromAccessToken(token: String): UserAccount {
        if (!isAccessTokenValid(token)) {
            throw ServiceException(HttpStatus.UNAUTHORIZED.value(), "접근 토큰이 유효하지 않습니다.")
        }
        val claims = JwtUtils.getTokenPayload(secret, token)
        val username = claims["username"].toString()
        return getUserByUsername(username)
    }

    /**
     * 주어진 유저 ID에 해당하는 회원의 리프레시 토큰을 재발급한다.
     *
     * @param username 리프레시 토큰을 재발급할 회원의 유저 ID
     * @return 재발급된 리프레시 토큰
     */
    fun rotateRefreshTokenOfUser(username: String): String {
        val userAccount = getUserByUsername(username)
        userAccount.refreshToken = UuidUtils.uuidV4ToBase64String(UUID.randomUUID())
        userAccount.refreshTokenExpiresAt = LocalDateTime.now().plusDays(refreshExpireDays.toLong())
        return userAccount.refreshToken?: ""
    }

    /**
     * 주어진 접근 토큰의 유효성을 검증한다.
     *
     * @param token 검증할 접근 토큰
     * @return 유효할시 {@code true}
     */
    private fun isAccessTokenValid(token: String): Boolean {
        return JwtUtils.isTokenValid(secret, token)
    }

    /**
     * 주어진 회원의 리프레시 토큰의 유효기간을 검증한다.
     *
     * @param userAccount 리프레시 토큰의 유효기간을 검증할 회원
     * @return 유효한시 {@code true}
     */
    private fun isRefreshTokenFresh(userAccount: UserAccount): Boolean {
        return userAccount.refreshTokenExpiresAt?.isAfter(LocalDateTime.now()) ?: false
    }

    /**
     * 주어진 리프레시 토큰과 주어진 회원의 리프레시 토큰을 비교, 검증한다.
     *
     * @param userAccount        리프레시 토큰의 소유자
     * @param refreshTokenBase64 검증하고자 하는 리프레시 토큰
     * @return 유효할시 {@code true}
     */
    private fun isRefreshTokenValid(userAccount: UserAccount, refreshTokenBase64: String): Boolean {
        return userAccount.refreshToken.equals(refreshTokenBase64)
    }

    /**
     * 주어진 유저 ID에 해당하는 회원을 반환한다.
     *
     * @param username 조회할 회원의 유저 ID
     * @return 회원 오브젝트
     */
    private fun getUserByUsername(username: String): UserAccount {
        return userAccountRepository.findByUsername(username)?:
            throw ServiceException(HttpStatus.NOT_FOUND.value(), "해당 사용자(ID: ${username})를 찾을 수 없습니다.")
    }

    /**
     * 주어진 리프레시 토큰을 소유하고 있는 회원을 반환한다.
     *
     * @param refreshToken 조회할 회원의 리프레시 토큰
     * @return 회원 오브젝트
     */
    private fun getUserByRefreshToken(refreshToken: String): UserAccount {
        return userAccountRepository.findByRefreshToken(refreshToken)?:
            throw ServiceException(HttpStatus.NOT_FOUND.value(), "해당 리프레시 토큰을 가진 유저를 찾을 수 없습니다.")
    }
}