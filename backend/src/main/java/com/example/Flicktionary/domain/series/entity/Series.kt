package com.example.Flicktionary.domain.series.entity

import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.genre.entity.Genre
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Series(
    @Column(unique = true)
    val tmdbId: Long,
    @Column(nullable = false)
    val title: String,
    @Column(columnDefinition = "TEXT")
    val overview: String,
    val releaseStartDate: LocalDate?,
    val releaseEndDate: LocalDate?,
    val status: String,
    val posterPath: String?,
    val episodeNumber: Int,
    val productionCountry: String?,
    val productionCompany: String?
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

    @ManyToMany
    @JoinTable(
        name = "series_genre",
        joinColumns = [JoinColumn(name = "series_id")],
        inverseJoinColumns = [JoinColumn(name = "genre_id")]
    )
    val genres: MutableList<Genre> = mutableListOf()

    @OneToMany(mappedBy = "series", cascade = [CascadeType.ALL], orphanRemoval = true)
    val casts: MutableList<SeriesCast> = mutableListOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    var director: Director? = null
}
