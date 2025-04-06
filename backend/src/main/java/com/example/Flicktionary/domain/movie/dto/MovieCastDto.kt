package com.example.Flicktionary.domain.movie.dto

import com.example.Flicktionary.domain.actor.dto.ActorDto
import com.example.Flicktionary.domain.movie.entity.MovieCast

data class MovieCastDto(
    val actor: ActorDto,
    val characterName: String
) {
    constructor(movieCast: MovieCast) : this(
        ActorDto(movieCast.actor),
        movieCast.characterName
    )
}
