package com.example.Flicktionary.domain.movie.controller

import com.example.Flicktionary.domain.movie.dto.MovieRequest
import com.example.Flicktionary.domain.movie.dto.MovieResponse
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail
import com.example.Flicktionary.domain.movie.service.MovieService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
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

    @Operation(summary = "영화 생성", description = "영화를 생성합니다. 관리자만 접근 가능합니다.")
    @PostMapping
    fun createMovie(@RequestBody @Valid request: MovieRequest): ResponseEntity<ResponseDto<MovieResponseWithDetail>> {
        val response = movieService.createMovie(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseDto.of(HttpStatus.CREATED.value().toString(), HttpStatus.CREATED.reasonPhrase, response))
    }

    @Operation(summary = "영화 수정", description = "영화를 수정합니다. 관리자만 접근 가능합니다.")
    @PutMapping("/{id}")
    fun updateMovie(
        @PathVariable id: Long,
        @RequestBody @Valid request: MovieRequest
    ): ResponseEntity<ResponseDto<MovieResponseWithDetail>> {
        val response = movieService.updateMovie(id, request)
        return ResponseEntity.ok(ResponseDto.ok(response))
    }

    @Operation(summary = "영화 삭제", description = "영화를 삭제합니다. 관리자만 접근 가능합니다.")
    @DeleteMapping("/{id}")
    fun deleteMovie(@PathVariable id: Long): ResponseEntity<ResponseDto<Void>> {
        movieService.deleteMovie(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ResponseDto.of(HttpStatus.NO_CONTENT.value().toString(), HttpStatus.NO_CONTENT.reasonPhrase, null))
    }
}
