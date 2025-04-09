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
}
