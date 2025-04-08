package com.example.Flicktionary.domain.genre.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Genre(
    @Id
    @Column(nullable = false)
    val id: Long,

    @Column(nullable = false)
    val name: String
)