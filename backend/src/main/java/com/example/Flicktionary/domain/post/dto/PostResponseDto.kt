package com.example.Flicktionary.domain.post.dto

import com.example.Flicktionary.domain.post.entity.Post
import java.time.LocalDateTime

data class PostResponseDto(
    val id: Long? = null,
    val userAccountId: Long? = null,
    val nickname: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime? = null,
    val isSpoiler: Boolean? = null
) {
    companion object {
        @JvmStatic
        fun fromEntity(post: Post): PostResponseDto {
            // 유저가 탈퇴할 경우 "탈퇴한 회원"으로 처리
            val nickname = post.userAccount?.nickname ?: "탈퇴한 회원"
            val userAccountId = post.userAccount?.id

            return PostResponseDto(
                id = post.id,
                userAccountId = userAccountId,
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                isSpoiler = post.isSpoiler,
                nickname = nickname
            )
        }
    }
}
