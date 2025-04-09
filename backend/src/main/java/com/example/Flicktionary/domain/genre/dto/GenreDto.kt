package com.example.Flicktionary.domain.genre.dto

import com.example.Flicktionary.domain.genre.entity.Genre

data class GenreDto(
    val id: Long,
    val name: String
) {
    constructor(genre: Genre) : this(
        genre.id,
        genre.name
    )
}