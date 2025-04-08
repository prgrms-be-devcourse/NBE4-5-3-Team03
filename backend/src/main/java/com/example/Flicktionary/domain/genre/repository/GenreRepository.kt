package com.example.Flicktionary.domain.genre.repository

import com.example.Flicktionary.domain.genre.entity.Genre
import org.springframework.data.jpa.repository.JpaRepository

interface GenreRepository : JpaRepository<Genre, Long> {
    fun findByName(genreName: String): Genre?
}