package com.example.Flicktionary.domain.review.dto

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.review.entity.Review
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.user.entity.UserAccount

data class ReviewDto(
    val id: Long? = null,
    val userAccountId: Long? = null,
    val nickname: String? = null,
    val movieId: Long? = null,
    val seriesId: Long? = null,
    val rating: Int,
    val content: String
) {
    companion object {
        // Entity를 DTO로 변환
        fun fromEntity(review: Review): ReviewDto {
            return ReviewDto(
                id = review.id,
                userAccountId = review.userAccount?.id,
                nickname = review.userAccount?.nickname ?: "탈퇴한 회원",
                movieId = review.movie?.id,
                seriesId = review.series?.id,
                rating = review.rating,
                content = review.content
            )
        }
    }

    // DTO를 Entity로 변환
    fun toEntity(userAccount: UserAccount? = null, movie: Movie? = null, series: Series? = null): Review {
        return Review(
            id = this.id,
            userAccount = userAccount,
            movie = movie,
            series = series,
            rating = this.rating,
            content = this.content
        )
    }
}
