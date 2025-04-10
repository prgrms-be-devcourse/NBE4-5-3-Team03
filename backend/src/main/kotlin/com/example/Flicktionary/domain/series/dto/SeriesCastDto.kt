package com.example.Flicktionary.domain.series.dto

import com.example.Flicktionary.domain.actor.dto.ActorDto
import com.example.Flicktionary.domain.series.entity.SeriesCast

data class SeriesCastDto(
    val actor: ActorDto,
    val characterName: String
) {
    constructor(seriesCast: SeriesCast) : this(
        ActorDto(seriesCast.actor),
        seriesCast.characterName
    )
}
