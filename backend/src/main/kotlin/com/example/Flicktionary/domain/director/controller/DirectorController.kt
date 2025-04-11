package com.example.Flicktionary.domain.director.controller

import com.example.Flicktionary.domain.actor.controller.ActorController
import com.example.Flicktionary.domain.director.dto.DirectorDto
import com.example.Flicktionary.domain.director.dto.DirectorRequest
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.service.DirectorService
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/directors")
class DirectorController(
    private val directorService: DirectorService
) {

    @GetMapping
    fun getDirectors(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<DirectorDto>>> {
        val directorPage = directorService.getDirectors(keyword, page, pageSize)
        val directorDtoPage = directorPage.map { DirectorDto(it) }
        return ResponseEntity.ok(ResponseDto.ok(PageDto(directorDtoPage)))
    }

    @GetMapping("/{id}")
    fun getDirector(@PathVariable id: Long): ResponseEntity<ResponseDto<DirectorResponse>> {
        val director = directorService.getDirector(id)
        val movies = directorService.getMoviesByDirectorId(id)
        val series = directorService.getSeriesByDirectorId(id)

        return ResponseEntity.ok(ResponseDto.ok(DirectorResponse(director, movies, series)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createDirector(@Valid @RequestBody request: DirectorRequest): ResponseEntity<ResponseDto<DirectorDto>> {
        val director = directorService.createDirector(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ResponseDto.of(
                    HttpStatus.CREATED.value().toString(),
                    HttpStatus.CREATED.reasonPhrase,
                    DirectorDto(director)
                )
            )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateDirector(
        @PathVariable id: Long,
        @Valid @RequestBody request: DirectorRequest
    ): ResponseEntity<ResponseDto<DirectorDto>> {
        val director = directorService.updateDirector(id, request)
        return ResponseEntity.ok(ResponseDto.ok(DirectorDto(director)))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteDirector(@PathVariable id: Long): ResponseEntity<ResponseDto<Nothing>> {
        directorService.deleteDirector(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(
                ResponseDto.of(
                    HttpStatus.NO_CONTENT.value().toString(),
                    HttpStatus.NO_CONTENT.reasonPhrase
                )
            )
    }

    data class DirectorResponse(
        val id: Long,
        val name: String,
        val profilePath: String?,
        val movies: List<ActorController.MovieDTO>,
        val series: List<ActorController.SeriesDTO>
    ) {
        constructor(director: Director, movies: List<Movie>, series: List<Series>) : this(
            id = director.id,
            name = director.name,
            profilePath = director.profilePath,
            movies = movies.map { ActorController.MovieDTO(it) },
            series = series.map { ActorController.SeriesDTO(it) }
        )
    }
}