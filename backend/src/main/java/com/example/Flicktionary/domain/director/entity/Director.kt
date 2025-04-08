package com.example.Flicktionary.domain.director.entity

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import jakarta.persistence.*

@Entity
class Director(
    @Id
    val id: Long,

    @Column(nullable = false)
    val name: String,

    val profilePath: String? = null,

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val movies: MutableList<Movie> = mutableListOf(),

    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val series: MutableList<Series> = mutableListOf()
) {
    constructor(id: Long, name: String, profilePath: String?) : this(
        id = id,
        name = name,
        profilePath = profilePath,
        movies = mutableListOf(),
        series = mutableListOf()
    )
}