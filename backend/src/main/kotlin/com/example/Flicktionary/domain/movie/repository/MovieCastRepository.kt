package com.example.Flicktionary.domain.movie.repository

import com.example.Flicktionary.domain.movie.entity.MovieCast
import org.springframework.data.jpa.repository.JpaRepository

interface MovieCastRepository : JpaRepository<MovieCast, Long> {
    fun findMoviesByActorId(actorId: Long): List<MovieCast>
}
