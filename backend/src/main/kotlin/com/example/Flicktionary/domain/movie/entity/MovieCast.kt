package com.example.Flicktionary.domain.movie.entity

import com.example.Flicktionary.domain.actor.entity.Actor
import jakarta.persistence.*

@Entity
class MovieCast(
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    val movie: Movie,
    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    val actor: Actor,
    @Column(length = 500)
    val characterName: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var _id: Long? = null;

    var id: Long
        get() = _id ?: 0
        set(value) {
            _id = value
        }
}
