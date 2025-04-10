package com.example.Flicktionary.domain.director.dto

import com.example.Flicktionary.domain.director.entity.Director

data class DirectorDto(
    val id: Long,
    val name: String,
    val profilePath: String?
) {
    constructor(director: Director) : this(
        id = director.id,
        name = director.name,
        profilePath = director.profilePath
    )
}
