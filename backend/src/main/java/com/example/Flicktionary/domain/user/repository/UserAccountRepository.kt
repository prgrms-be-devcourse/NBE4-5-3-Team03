package com.example.Flicktionary.domain.user.repository

import com.example.Flicktionary.domain.user.entity.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 회원 엔티티 리포지토리
 */
@Repository
interface UserAccountRepository: JpaRepository<UserAccount, Long> {
    fun findByUsername(username: String): UserAccount?
    fun findByRefreshToken(refreshToken: String): UserAccount?
}