package com.example.Flicktionary.domain.post.dto

import com.example.Flicktionary.domain.post.entity.Post
import com.example.Flicktionary.domain.user.entity.UserAccount
import java.time.LocalDateTime

data class PostCreateRequestDto(
    var userAccountId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var isSpoiler: Boolean? = null
) {

    fun toEntity(userAccount: UserAccount): Post {
        return Post(
            userAccount = userAccount,
            title = this.title ?: "", // nullable일 경우 빈 문자열 할당
            content = this.content ?: "", // nullable일 경우 빈 문자열 할당
            isSpoiler = this.isSpoiler ?: false, // nullable일 경우 false로 할당
            createdAt = LocalDateTime.now()
        )
    }
}
