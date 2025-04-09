package com.example.Flicktionary.domain.movie.entity

import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.genre.entity.Genre
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Movie(
    @Column(nullable = false)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var overview: String,
    var releaseDate: LocalDate?,
    var status: String,
    var posterPath: String?,
    var runtime: Int,
    var productionCountry: String,
    var productionCompany: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var _id: Long? = null

    var id: Long
        get() = _id ?: 0
        set(value) {
            _id = value
        }

    var averageRating = 0.0

    var ratingCount = 0

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genre",
        joinColumns = [JoinColumn(name = "movie_id")],
        inverseJoinColumns = [JoinColumn(name = "genre_id")]
    )
    val genres: MutableList<Genre> = mutableListOf()

    @OneToMany(mappedBy = "movie", cascade = [CascadeType.ALL], orphanRemoval = true)
    val casts: MutableList<MovieCast> = mutableListOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    var director: Director? = null
}
