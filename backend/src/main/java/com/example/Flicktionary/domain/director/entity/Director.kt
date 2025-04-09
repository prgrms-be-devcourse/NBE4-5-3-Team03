package com.example.Flicktionary.domain.director.entity

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import jakarta.persistence.*

@Entity
class Director(
    @Column(nullable = false)
    val name: String,

    val profilePath: String? = null,

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val movies: MutableList<Movie> = mutableListOf(),

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val series: MutableList<Series> = mutableListOf()
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

    constructor(name: String, profilePath: String?) : this(
        name = name,
        profilePath = profilePath,
        movies = mutableListOf(),
        series = mutableListOf()
    )
}