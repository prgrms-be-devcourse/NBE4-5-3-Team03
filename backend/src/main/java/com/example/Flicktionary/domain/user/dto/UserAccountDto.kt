package com.example.Flicktionary.domain.user.dto

import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType

/**
 * {@link UserAccount}에 해당하는 DTO.
 *
 * @param id
 * @param username
 * @param password
 * @param email
 * @param nickname
 * @param role
 */
data class UserAccountDto(
    val id: Long,
    val username: String,
    val password: String,
    val email: String,
    val nickname: String,
    val role: String
) {
    companion object {
        @JvmStatic
        fun from(userAccount: UserAccount): UserAccountDto {
            return UserAccountDto(
                userAccount.id ?: 0L,
                userAccount.username,
                userAccount.password,
                userAccount.email,
                userAccount.nickname,
                userAccount.role.toString()
            )
        }
    }

    fun toEntity(): UserAccount {
        return UserAccount(
            id,
            username,
            password,
            email,
            nickname,
            UserAccountType.valueOf(role)
        )
    }
}
