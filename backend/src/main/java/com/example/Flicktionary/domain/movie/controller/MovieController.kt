package com.example.Flicktionary.domain.movie.controller

import com.example.Flicktionary.domain.movie.dto.MovieResponse
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail
import com.example.Flicktionary.domain.movie.service.MovieService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
@Tag(name = "영화 API", description = "영화 API입니다.")
class MovieController(
    private val movieService: MovieService
) {
    @Operation(summary = "영화 목록 조회", description = "영화 목록을 조회합니다. (페이징, 검색, 정렬 지원)")
    @GetMapping
    fun getMovies(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy: String
    ): ResponseEntity<ResponseDto<PageDto<MovieResponse>>> {
        val response = movieService.getMovies(keyword, page, pageSize, sortBy)
        return ResponseEntity.ok(ResponseDto.ok(response))
    }

    @Operation(summary = "영화 상세 조회", description = "영화 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    fun getMovie(@PathVariable id: Long): ResponseEntity<ResponseDto<MovieResponseWithDetail>> {
        val response = movieService.getMovie(id)
        return ResponseEntity.ok(ResponseDto.ok(response))
    }
}
