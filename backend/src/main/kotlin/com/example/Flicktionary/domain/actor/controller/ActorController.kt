package com.example.Flicktionary.domain.actor.controller

import com.example.Flicktionary.domain.actor.dto.ActorDto
import com.example.Flicktionary.domain.actor.dto.ActorRequest
import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.service.ActorService
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
@RequestMapping("/api/actors")
class ActorController(
    private val actorService: ActorService
) {
    @GetMapping("/{actorId}")
    fun getActorWithMovies(@PathVariable actorId: Long): ResponseEntity<ResponseDto<ActorResponse>> {
        val actor = actorService.getActorById(actorId)
        val movies = actorService.getMoviesByActorId(actorId)
        val series = actorService.getSeriesByActorId(actorId)

        return ResponseEntity.ok(ResponseDto.ok(ActorResponse(actor, movies, series)))
    }

    @GetMapping
    fun getActors(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<ActorDto>>> {
        val actorPage = actorService.getActors(keyword, page, pageSize)
        val actorDtoPage = actorPage.map { ActorDto(it) }

        return ResponseEntity.ok(ResponseDto.ok(PageDto(actorDtoPage)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createActor(@Valid @RequestBody request: ActorRequest): ResponseEntity<ResponseDto<ActorDto>> {
        val actor = actorService.createActor(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ResponseDto.of(
                    HttpStatus.CREATED.value().toString(),
                    HttpStatus.CREATED.reasonPhrase,
                    ActorDto(actor)
                )
            )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateActor(
        @PathVariable id: Long,
        @Valid @RequestBody request: ActorRequest
    ): ResponseEntity<ResponseDto<ActorDto>> {
        val actor = actorService.updateActor(id, request)
        return ResponseEntity.ok(ResponseDto.ok(ActorDto(actor)))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteActor(@PathVariable id: Long): ResponseEntity<ResponseDto<Nothing>> {
        actorService.deleteActor(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(
                ResponseDto.of(
                    HttpStatus.NO_CONTENT.value().toString(),
                    HttpStatus.NO_CONTENT.reasonPhrase
                )
            )
    }

    // DTO 클래스 (내부 클래스로 정의 가능)
    data class ActorResponse(
        val id: Long,
        val name: String,
        val profilePath: String?,
        val movies: List<MovieDTO>,
        val series: List<SeriesDTO>
    ) {
        constructor(actor: Actor, movies: List<Movie>, series: List<Series>) : this(
            id = actor.id,
            name = actor.name,
            profilePath = actor.profilePath,
            movies = movies.map { MovieDTO(it) },
            series = series.map { SeriesDTO(it) }
        )
    }

    data class MovieDTO(
        val id: Long,
        val title: String,
        val posterPath: String?,
        val releaseDate: String?
    ) {
        constructor(movie: Movie) : this(
            id = movie.id,
            title = movie.title,
            posterPath = movie.posterPath,
            releaseDate = movie.releaseDate?.toString()
        )
    }

    data class SeriesDTO(
        val id: Long,
        val title: String,
        val posterPath: String?,
        val releaseStartDate: String?,
        val releaseEndDate: String?
    ) {
        constructor(series: Series) : this(
            id = series.id,
            title = series.title,
            posterPath = series.posterPath,
            releaseStartDate = series.releaseStartDate?.toString(),
            releaseEndDate = series.releaseEndDate?.toString()
        )
    }
}
