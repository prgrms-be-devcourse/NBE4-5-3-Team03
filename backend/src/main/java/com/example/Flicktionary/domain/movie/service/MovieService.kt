package com.example.Flicktionary.domain.movie.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.movie.dto.MovieResponse
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.entity.MovieCast
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.tmdb.service.TmdbService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@RequiredArgsConstructor
class MovieService(
    private val movieRepository: MovieRepository,
    private val tmdbService: TmdbService,
    private val genreRepository: GenreRepository,
    private val actorRepository: ActorRepository,
    private val directorRepository: DirectorRepository
) {
    private val BASE_IMAGE_URL = "https://image.tmdb.org/t/p"

    @Transactional(readOnly = true)
    fun getMovies(keyword: String, page: Int, pageSize: Int, sortBy: String): PageDto<MovieResponse> {
        val sort = getSort(sortBy)
        val pageable: Pageable = PageRequest.of(page - 1, pageSize, sort)
        val formattedKeyword = keyword.lowercase(Locale.getDefault()).replace(" ".toRegex(), "")

        val movies = movieRepository.findByTitleLike(formattedKeyword, pageable)

        return PageDto(movies.map { movie: Movie ->
            MovieResponse(
                movie
            )
        })
    }

    fun getSort(sortBy: String): Sort {
        return when (sortBy) {
            "id" -> Sort.by(Sort.Direction.ASC, "id")
            "rating" -> Sort.by(Sort.Direction.DESC, "averageRating")
            "ratingCount" -> Sort.by(Sort.Direction.DESC, "ratingCount")
            else -> throw ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬 기준입니다.")
        }
    }

    @Transactional(readOnly = true)
    fun getMovie(id: Long): MovieResponseWithDetail {
        // fetch join을 이용해서 영화에 연관된 배우와 감독 정보를 가져옵니다.
        // 장르는 lazy loading
        val movie: Movie = movieRepository.findByIdWithCastsAndDirector(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 영화를 찾을 수 없습니다.")

        return MovieResponseWithDetail(movie)
    }

    // tmdb api를 이용해서 영화 정보를 받아와 저장합니다.
    @Transactional
    fun fetchAndSaveMovies(pages: Int) {
        val moviesToSave: MutableList<Movie> = mutableListOf()
        val genreCache: MutableMap<Long, Genre> = mutableMapOf()
        val actorCache: MutableMap<Long, Actor> = mutableMapOf()
        val directorCache: MutableMap<Long, Director> = mutableMapOf()

        for (i in 1..pages) {
            val movieDtos = tmdbService.fetchMovies(i)

            // 먼저 중복을 방지하기 위해 저장된 영화 ID를 조회
            val existingMovieIds = movieRepository.findAllTmdbIds()

            for (movieDto in movieDtos) {
                if (existingMovieIds.contains(movieDto.tmdbId) ||
                    moviesToSave.stream().anyMatch { s: Movie -> s.tmdbId == (movieDto.tmdbId) }
                ) {
                    continue  // 이미 존재하는 영화면 스킵
                }

                val movie = Movie(
                    movieDto.tmdbId,
                    movieDto.title,
                    movieDto.overview,
                    if (movieDto.releaseDate == null || movieDto.releaseDate.isEmpty()) null else LocalDate.parse(
                        movieDto.releaseDate
                    ),
                    movieDto.status,
                    if (movieDto.posterPath == null) null else BASE_IMAGE_URL + "/w342" + movieDto.posterPath,
                    movieDto.runtime,
                    if (movieDto.productionCountries.isEmpty()) null else movieDto.productionCountries[0].name,
                    if (movieDto.productionCompanies.isEmpty()) null else movieDto.productionCompanies[0].name
                )

                // 장르 저장 (캐싱 활용)
                for (tmdbGenre in movieDto.genres) {
                    val genre = genreCache.computeIfAbsent(tmdbGenre.id) { id: Long ->
                        genreRepository.findByIdOrNull(id) ?: genreRepository.save(Genre(id, tmdbGenre.name))
                    }
                    movie.genres.add(genre)
                }

                // 배우 저장 (캐싱 활용)
                for (tmdbActor in movieDto.credits.cast.stream().limit(5).toList()) {
                    val actor = actorCache.computeIfAbsent(tmdbActor.id) { id: Long ->
                        actorRepository.findByIdOrNull(id) ?: actorRepository.save(
                            Actor(
                                id,
                                tmdbActor.name,
                                tmdbActor.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                            )
                        )
                    }

                    val movieCast = MovieCast(movie, actor, tmdbActor.character)
                    movie.casts.add(movieCast)
                }

                // 감독 저장 (캐싱 활용)
                for (crew in movieDto.credits.crew) {
                    if (crew.job.equals("Director", ignoreCase = true)) {
                        val director = directorCache.computeIfAbsent(crew.id) { id: Long ->
                            directorRepository.findByIdOrNull(id) ?: directorRepository.save(
                                Director(
                                    id,
                                    crew.name,
                                    crew.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                                )
                            )
                        }
                        movie.director = director

                        if (!director.movies.contains(movie)) {
                            director.movies.add(movie)
                        }
                    }
                }

                moviesToSave.add(movie)
            }
        }

        if (moviesToSave.isNotEmpty()) {
            movieRepository.saveAll(moviesToSave)
        }
    }
}
