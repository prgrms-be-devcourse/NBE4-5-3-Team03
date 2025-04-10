package com.example.Flicktionary.domain.genre.entity

import jakarta.persistence.*

@Entity
class Genre(
    @Column(nullable = false)
    val name: String
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