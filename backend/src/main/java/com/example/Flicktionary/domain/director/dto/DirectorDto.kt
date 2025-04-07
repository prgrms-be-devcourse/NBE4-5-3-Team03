package com.example.Flicktionary.domain.director.dto

import com.example.Flicktionary.domain.director.entity.Director

data class DirectorDto(
    val id: Long,
    val name: String,
    val profilePath: String?
) {
    constructor(director: Director) : this(
        director.id,
        director.name,
        profilePath = director.profilePath?.let { "https://image.tmdb.org/t/p/w$it" }
    )
}
