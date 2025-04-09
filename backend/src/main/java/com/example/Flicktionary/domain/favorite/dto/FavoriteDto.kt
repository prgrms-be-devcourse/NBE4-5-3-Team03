package com.example.Flicktionary.domain.favorite.dto

import com.example.Flicktionary.domain.favorite.entity.ContentType
import com.example.Flicktionary.domain.favorite.entity.Favorite
import com.example.Flicktionary.domain.user.entity.UserAccount

data class FavoriteDto(
    val id: Long,
    val userId: Long,
    val contentType: ContentType,
    val contentId: Long,
    val data: Any? = null // MovieDto or SeriesDto
) {
    companion object {
        // Entity → DTO
        fun fromEntity(favorite: Favorite): FavoriteDto {
            val contentData: Any? = when (favorite.contentType) {
                ContentType.MOVIE -> favorite.movie?.let { FavoriteContentDto.fromMovie(it) }
                ContentType.SERIES -> favorite.series?.let { FavoriteContentDto.fromSeries(it) }
            }

            return FavoriteDto(
                id = favorite.id,
                userId = favorite.userAccount.id?: 0,
                contentType = favorite.contentType,
                contentId = favorite.contentId,
                data = contentData
            )
        }

        @JvmStatic
        fun builder(): Builder = Builder()
    }

    // DTO → Entity
    fun toEntity(user: UserAccount): Favorite {
        return Favorite(
            id = id,
            userAccount = user,
            contentType = contentType,
            contentId = contentId
        )
    }

    class Builder {
        private var id: Long = 0
        private var userId: Long = 0
        private var contentType: ContentType = ContentType.MOVIE
        private var contentId: Long = 0
        private var data: Any? = null

        fun id(id: Long) = apply { this.id = id }
        fun userId(userId: Long) = apply { this.userId = userId }
        fun contentType(contentType: ContentType) = apply { this.contentType = contentType }
        fun contentId(contentId: Long) = apply { this.contentId = contentId }
        fun data(data: Any?) = apply { this.data = data }

        fun build(): FavoriteDto {
            return FavoriteDto(
                id = id,
                userId = userId,
                contentType = contentType,
                contentId = contentId,
                data = data
            )
        }
    }
}
