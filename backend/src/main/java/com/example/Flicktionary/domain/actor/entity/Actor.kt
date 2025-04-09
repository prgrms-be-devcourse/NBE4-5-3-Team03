package com.example.Flicktionary.domain.actor.entity

import jakarta.persistence.*

@Entity
class Actor(
    @Column(nullable = false)
    val name: String,

    val profilePath: String?
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
}