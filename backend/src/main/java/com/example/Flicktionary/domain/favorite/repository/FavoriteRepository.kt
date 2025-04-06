package com.example.Flicktionary.domain.favorite.repository

import com.example.Flicktionary.domain.favorite.entity.ContentType
import com.example.Flicktionary.domain.favorite.entity.Favorite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FavoriteRepository : JpaRepository<Favorite?, Long?> {
    fun existsByUserAccountIdAndContentTypeAndContentId(
        userId: Long,
        contentType: ContentType,
        contentId: Long
    ): Boolean

    @Query(
        value = ("SELECT f FROM Favorite f " +
                "LEFT JOIN FETCH f.movie " +
                "LEFT JOIN FETCH f.series " +
                "WHERE f.userAccount.id = :userId"),
        countQuery = "SELECT COUNT(f) FROM Favorite f WHERE f.userAccount.id = :userId"
    )
    fun findAllByUserAccountIdWithContent(@Param("userId") userId: Long, pageable: Pageable): Page<Favorite>
}
