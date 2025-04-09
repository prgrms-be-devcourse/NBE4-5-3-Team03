package com.example.Flicktionary.domain.movie.service

import com.example.Flicktionary.domain.actor.entity.Actor
import com.example.Flicktionary.domain.actor.repository.ActorRepository
import com.example.Flicktionary.domain.director.entity.Director
import com.example.Flicktionary.domain.director.repository.DirectorRepository
import com.example.Flicktionary.domain.genre.entity.Genre
import com.example.Flicktionary.domain.genre.repository.GenreRepository
import com.example.Flicktionary.domain.movie.dto.MovieRequest
import com.example.Flicktionary.domain.movie.dto.MovieResponse
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail
import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.entity.MovieCast
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.tmdb.service.TmdbService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
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
class MovieService(
    private val movieRepository: MovieRepository,
    private val tmdbService: TmdbService,
    private val genreRepository: GenreRepository,
    private val actorRepository: ActorRepository,
    private val directorRepository: DirectorRepository
) {
    private val BASE_IMAGE_URL = "https://image.tmdb.org/t/p"

    @Transactional
    fun createMovie(request: MovieRequest): MovieResponseWithDetail {
        val movie = Movie(
            title = request.title,
            overview = request.overview,
            releaseDate = request.releaseDate,
            status = request.status,
            posterPath = request.posterPath,
            runtime = request.runtime,
            productionCountry = request.productionCountry,
            productionCompany = request.productionCompany
        )

        // 장르 추가
        val genres = genreRepository.findAllById(request.genreIds)
        movie.genres.addAll(genres)

        // 배우 추가
        val casts = request.casts.map {
            val actor = actorRepository.findByIdOrNull(it.actorId)
                ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${it.actorId}번 배우를 찾을 수 없습니다.")
            MovieCast(movie, actor, it.characterName)
        }
        movie.casts.addAll(casts)

        // 감독 설정
        val director = directorRepository.findByIdOrNull(request.directorId)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${request.directorId}번 감독을 찾을 수 없습니다.")
        movie.director = director

        val savedMovie = movieRepository.save(movie)
        return MovieResponseWithDetail(savedMovie)
    }

    @Transactional
    fun updateMovie(id: Long, request: MovieRequest): MovieResponseWithDetail {
        val movie = movieRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 영화를 찾을 수 없습니다.")

        movie.apply {
            title = request.title
            overview = request.overview
            releaseDate = request.releaseDate
            status = request.status
            posterPath = request.posterPath
            runtime = request.runtime
            productionCountry = request.productionCountry
            productionCompany = request.productionCompany
        }

        val genres = genreRepository.findAllById(request.genreIds)
        movie.genres.clear()
        movie.genres.addAll(genres)

        val casts = request.casts.map {
            val actor = actorRepository.findByIdOrNull(it.actorId)
                ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${it.actorId}번 배우를 찾을 수 없습니다.")
            MovieCast(movie, actor, it.characterName)
        }
        movie.casts.clear()
        movie.casts.addAll(casts)

        val director = directorRepository.findByIdOrNull(request.directorId)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${request.directorId}번 감독을 찾을 수 없습니다.")
        movie.director = director

        return MovieResponseWithDetail(movie)
    }

    @Transactional
    fun deleteMovie(id: Long) {
        val movie = movieRepository.findByIdOrNull(id)
            ?: throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 영화를 찾을 수 없습니다.")
        movieRepository.delete(movie)
    }

    @Transactional(readOnly = true)
    fun getMovies(keyword: String, page: Int, pageSize: Int, sortBy: String): PageDto<MovieResponse> {
        val sort = getSort(sortBy)
        val pageable: Pageable = PageRequest.of(page - 1, pageSize, sort)
        val formattedKeyword = keyword.lowercase(Locale.getDefault()).replace(" ".toRegex(), "")

        val movies = movieRepository.findByTitleLike(formattedKeyword, pageable)

        return PageDto(movies.map { movie: Movie -> MovieResponse(movie) })
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
        val moviesToSave = mutableListOf<Movie>()
        val existingMovieKeys = mutableSetOf<Pair<String, LocalDate?>>()

        for (i in 1..pages) {
            val movieDtos = tmdbService.fetchMovies(i)

            for (movieDto in movieDtos) {
                val releaseDate = if (movieDto.releaseDate.isEmpty()) null else LocalDate.parse(movieDto.releaseDate)
                val movieKey = movieDto.title to releaseDate

                // 이미 저장된 영화인지 확인
                if (existingMovieKeys.contains(movieKey)) {
                    continue
                }
                existingMovieKeys.add(movieKey)

                val movie = Movie(
                    movieDto.title,
                    movieDto.overview,
                    releaseDate,
                    movieDto.status,
                    movieDto.posterPath?.let { "$BASE_IMAGE_URL/w185$it" },
                    movieDto.runtime,
                    movieDto.productionCountries.firstOrNull()?.name ?: "",
                    movieDto.productionCompanies.firstOrNull()?.name ?: ""
                )

                // 장르 추가
                for (tmdbGenre in movieDto.genres) {
                    val genre = genreRepository.findByIdOrNull(tmdbGenre.id)
                        ?: genreRepository.save(Genre(tmdbGenre.id, tmdbGenre.name))
                    movie.genres.add(genre)
                }

                // 배우 추가 (상위 5명)
                for (tmdbActor in movieDto.credits.cast.take(5)) {
                    val profilePath = tmdbActor.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                    val actor =
                        actorRepository.findByNameAndProfilePath(tmdbActor.name, profilePath) ?: actorRepository.save(
                            Actor(tmdbActor.name, profilePath)
                        )

                    movie.casts.add(MovieCast(movie, actor, tmdbActor.character))
                }

                // 감독 설정
                movieDto.credits.crew.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.let {
                    val profilePath = it.profilePath?.let { "$BASE_IMAGE_URL/w185$it" }
                    val director =
                        directorRepository.findByNameAndProfilePath(it.name, profilePath) ?: directorRepository.save(
                            Director(it.name, profilePath)
                        )

                    movie.director = director
                    if (!director.movies.contains(movie)) {
                        director.movies.add(movie)
                    }
                }

                moviesToSave.add(movie)
            }
        }

        movieRepository.saveAll(moviesToSave)
    }
}
