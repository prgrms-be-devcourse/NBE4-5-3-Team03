package com.example.Flicktionary.domain.genre.service

import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GenreService(
    private val genreRepository: GenreRepository
) {
    @Transactional(readOnly = true)
    fun getGenres(keyword: String): List<Genre> {
        val formattedKeyword = keyword.lowercase().replace(" ", "")
        return genreRepository.findByNameLike(formattedKeyword)
    }
}