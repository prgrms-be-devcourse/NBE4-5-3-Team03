package com.example.Flicktionary.domain.genre.service

import com.example.Flicktionary.domain.genre.dto.GenreRequest
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.http.HttpStatus
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

    @Transactional
    fun createGenre(request: GenreRequest): Genre {
        if (genreRepository.existsGenreByName(request.name)) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 장르입니다.")
        }

        val genre = Genre(request.name)
        return genreRepository.save(genre)
    }
}