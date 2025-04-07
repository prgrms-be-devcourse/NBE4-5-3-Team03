package com.example.Flicktionary.domain.series.repository

import com.example.Flicktionary.domain.series.entity.SeriesCast
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesCastRepository : JpaRepository<SeriesCast, Long> {
    fun findSeriesByActorId(actorId: Long): List<SeriesCast>
}
