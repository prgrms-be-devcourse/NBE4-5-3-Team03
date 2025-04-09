package com.example.Flicktionary.domain.actor.repository

import com.example.Flicktionary.domain.actor.entity.Actor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ActorRepository : JpaRepository<Actor, Long> {
    @Query("SELECT a FROM Actor a WHERE LOWER(REPLACE(a.name, ' ', '')) LIKE CONCAT('%', :keyword, '%')")
    fun findByNameLike(keyword: String, pageable: Pageable): Page<Actor>

    fun existsByNameAndProfilePath(name: String, profilePath: String?): Boolean
}
