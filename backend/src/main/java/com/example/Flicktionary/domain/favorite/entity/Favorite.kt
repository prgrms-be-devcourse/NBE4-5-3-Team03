package com.example.Flicktionary.domain.favorite.entity

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.user.entity.UserAccount
import jakarta.persistence.*

@Entity
@Table(
    name = "favorite",
    uniqueConstraints = [UniqueConstraint(
        name = "unique_favorite",
        columnNames = ["user_id", "content_type", "content_id"]
    )]
)

class Favorite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val userAccount: UserAccount,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val contentType: ContentType, // MOVIE, SERIES 구분

    @Column(nullable = false)
    val contentId: Long, // 영화 or 드라마 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentId", referencedColumnName = "id", insertable = false, updatable = false)
    val movie: Movie? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentId", referencedColumnName = "id", insertable = false, updatable = false)
    val series: Series? = null
){
    // 보조 생성자 (Java에서 사용 가능)
    constructor(
        id: Long,
        userAccount: UserAccount,
        contentType: ContentType,
        contentId: Long
    ) : this(
        id,
        userAccount,
        contentType,
        contentId,
        null, // movie
        null  // series
    )
}