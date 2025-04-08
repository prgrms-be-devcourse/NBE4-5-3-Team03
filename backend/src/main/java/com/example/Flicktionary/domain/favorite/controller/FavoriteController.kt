package com.example.Flicktionary.domain.favorite.controller

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto
import com.example.Flicktionary.domain.favorite.service.FavoriteService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService
) {

    // 즐겨찾기 추가
    @PostMapping
    fun createFavorite(@RequestBody favoriteDto: FavoriteDto): ResponseEntity<ResponseDto<FavoriteDto>> {
        val createdFavorite = favoriteService.createFavorite(favoriteDto)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ResponseDto.of(
                    HttpStatus.CREATED.value().toString(),
                    HttpStatus.CREATED.reasonPhrase,
                    createdFavorite
                )
            )
    }

    // 특정 사용자 ID의 즐겨찾기 목록 조회
    @GetMapping("/{userId}")
    fun getUserFavorites(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String
    ): ResponseEntity<ResponseDto<PageDto<FavoriteDto>>> {
        val favorites = favoriteService.getUserFavorites(userId, page, pageSize, sortBy, direction)
        return ResponseEntity.ok(ResponseDto.ok(favorites))
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/{id}")
    fun deleteFavorite(@PathVariable id: Long): ResponseEntity<ResponseDto<Void>> {
        favoriteService.deleteFavorite(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(
                ResponseDto.of(
                    HttpStatus.NO_CONTENT.value().toString(),
                    HttpStatus.NO_CONTENT.reasonPhrase,
                    null
                )
            )
    }
}
