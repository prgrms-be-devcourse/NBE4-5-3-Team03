package com.example.Flicktionary.domain.post.dto

data class PostUpdateRequestDto(
    var id: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var isSpoiler: Boolean? = null
)
