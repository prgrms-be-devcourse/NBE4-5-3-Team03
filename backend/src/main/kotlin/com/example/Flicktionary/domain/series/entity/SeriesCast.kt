package com.example.Flicktionary.domain.series.entity

import com.example.Flicktionary.domain.actor.entity.Actor
import jakarta.persistence.*

@Entity
class SeriesCast(
    @ManyToOne
    @JoinColumn(name = "series_id", nullable = false)
    val series: Series,
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
