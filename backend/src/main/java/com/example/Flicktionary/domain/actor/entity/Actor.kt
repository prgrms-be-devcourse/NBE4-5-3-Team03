package com.example.Flicktionary.domain.actor.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Actor(
    @Id
    val id: Long,

    @Column(nullable = false)
    val name: String,

    val profilePath: String?
)