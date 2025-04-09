package com.example.Flicktionary.domain.genre.controller

import com.example.Flicktionary.domain.genre.dto.GenreDto
import com.example.Flicktionary.domain.genre.service.GenreService
import com.example.Flicktionary.global.dto.ResponseDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/genres")
class GenreController(
    private val genreService: GenreService
) {
    @GetMapping
    fun getGenres(@Valid @NotBlank(message = "검색어를 입력해주세요.") @RequestParam keyword: String)
            : ResponseEntity<ResponseDto<List<GenreDto>>> {
        val genres = genreService.getGenres(keyword)

        return ResponseEntity.ok(ResponseDto.ok(genres.map { GenreDto(it) }))
    }
}