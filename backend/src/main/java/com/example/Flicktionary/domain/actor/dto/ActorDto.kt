package com.example.Flicktionary.domain.actor.dto

import com.example.Flicktionary.domain.actor.entity.Actor

data class ActorDto(
    val id: Long,
    val name: String,
    val profilePath: String?
) {
    constructor(actor: Actor) : this(actor.id, actor.name, actor.profilePath)
}