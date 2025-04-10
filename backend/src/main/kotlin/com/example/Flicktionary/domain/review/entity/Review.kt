package com.example.Flicktionary.domain.review.entity

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.user.entity.UserAccount
import jakarta.persistence.*

@Entity
@Table(name = "review")
class Review(
    // 리뷰 id (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 유저 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount? = null,

    // 영화 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    var movie: Movie? = null,

    // 드라마 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    var series: Series? = null,

    // 평점
    @Column(nullable = false)
    var rating: Int,

    // 리뷰
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String
) {
    constructor() : this(null, null, null, null, 0, "")
}
