package com.example.Flicktionary.domain.genre.repository

import com.example.Flicktionary.domain.genre.entity.Genre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GenreRepository : JpaRepository<Genre, Long> {
    fun findByName(genreName: String): Genre?

    @Query("SELECT g FROM Genre g WHERE LOWER(REPLACE(g.name, ' ', '')) LIKE CONCAT('%', :keyword, '%')")
    fun findByNameLike(keyword: String): List<Genre>

    fun existsGenreByName(name: String): Boolean
}