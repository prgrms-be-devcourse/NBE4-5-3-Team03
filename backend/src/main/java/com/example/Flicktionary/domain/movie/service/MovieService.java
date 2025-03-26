package com.example.Flicktionary.domain.movie.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.movie.dto.MovieResponse;
import com.example.Flicktionary.domain.movie.dto.MovieResponseWithDetail;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbMovieResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    private final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p";

    @Transactional(readOnly = true)
    public PageDto<MovieResponse> getMovies(String keyword, int page, int pageSize, String sortBy) {
        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        String formattedKeyword = keyword.toLowerCase().replaceAll(" ", "");

        Page<Movie> movies = movieRepository.findByTitleLike(formattedKeyword, pageable);

        return new PageDto<>(movies.map(MovieResponse::new));
    }

    public Sort getSort(String sortBy) {
        return switch (sortBy) {
            case "id" -> Sort.by(Sort.Direction.ASC, "id");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "ratingCount" -> Sort.by(Sort.Direction.DESC, "ratingCount");
            default -> throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬 기준입니다.");
        };
    }

    @Transactional(readOnly = true)
    public MovieResponseWithDetail getMovie(long id) {
        // fetch join을 이용해서 영화에 연관된 배우와 감독 정보를 가져옵니다.
        // 장르는 lazy loading
        Movie movie = movieRepository.findByIdWithCastsAndDirector(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 영화를 찾을 수 없습니다.".formatted(id)));

        return new MovieResponseWithDetail(movie);
    }

    // tmdb api를 이용해서 영화 정보를 받아와 저장합니다.
    @Transactional
    public void fetchAndSaveMovies(int pages) {
        List<Movie> moviesToSave = new ArrayList<>();
        Map<Long, Genre> genreCache = new HashMap<>();
        Map<Long, Actor> actorCache = new HashMap<>();
        Map<Long, Director> directorCache = new HashMap<>();

        for (int i = 1; i <= pages; i++) {
            List<TmdbMovieResponseWithDetail> movieDtos = tmdbService.fetchMovies(i);

            // 먼저 중복을 방지하기 위해 저장된 영화 ID를 조회
            Set<Long> existingMovieIds = movieRepository.findAllTmdbIds();

            for (TmdbMovieResponseWithDetail movieDto : movieDtos) {
                if (existingMovieIds.contains(movieDto.tmdbId()) ||
                        moviesToSave.stream().anyMatch(s -> s.getTmdbId().equals(movieDto.tmdbId()))) {
                    continue; // 이미 존재하는 영화면 스킵
                }

                Movie movie = Movie.builder()
                        .tmdbId(movieDto.tmdbId())
                        .title(movieDto.title())
                        .overview(movieDto.overview())
                        .releaseDate(movieDto.releaseDate() == null || movieDto.releaseDate().isEmpty()
                                ? null : LocalDate.parse(movieDto.releaseDate()))
                        .status(movieDto.status())
                        .posterPath(movieDto.posterPath() == null ? null : BASE_IMAGE_URL + "/w342" + movieDto.posterPath())
                        .runtime(movieDto.runtime())
                        .productionCountry(movieDto.productionCountries().isEmpty() ? null : movieDto.productionCountries().get(0).name())
                        .productionCompany(movieDto.productionCompanies().isEmpty() ? null : movieDto.productionCompanies().get(0).name())
                        .build();

                // 장르 저장 (캐싱 활용)
                for (TmdbMovieResponseWithDetail.TmdbGenre tmdbGenre : movieDto.genres()) {
                    Genre genre = genreCache.computeIfAbsent(tmdbGenre.id(), id ->
                            genreRepository.findById(id)
                                    .orElseGet(() -> genreRepository.save(new Genre(id, tmdbGenre.name()))));
                    movie.getGenres().add(genre);
                }

                // 배우 저장 (캐싱 활용)
                for (TmdbMovieResponseWithDetail.TmdbActor tmdbActor : movieDto.credits().cast().stream().limit(5).toList()) {
                    Actor actor = actorCache.computeIfAbsent(tmdbActor.id(), id ->
                            actorRepository.findById(id)
                                    .orElseGet(() -> actorRepository.save(new Actor(id, tmdbActor.name(),
                                            tmdbActor.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + tmdbActor.profilePath()))));

                    MovieCast movieCast = MovieCast.builder()
                            .movie(movie)
                            .actor(actor)
                            .characterName(tmdbActor.character())
                            .build();
                    movie.getCasts().add(movieCast);
                }

                // 감독 저장 (캐싱 활용)
                for (TmdbMovieResponseWithDetail.TmdbCrew crew : movieDto.credits().crew()) {
                    if (crew.job().equalsIgnoreCase("Director")) {
                        Director director = directorCache.computeIfAbsent(crew.id(), id ->
                                directorRepository.findById(id)
                                        .orElseGet(() -> directorRepository.save(new Director(id, crew.name(),
                                                crew.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + crew.profilePath()))));
                        movie.setDirector(director);

                        if (!director.getMovies().contains(movie)) {
                            director.getMovies().add(movie);
                        }
                    }
                }

                moviesToSave.add(movie);
            }
        }

        if (!moviesToSave.isEmpty()) {
            movieRepository.saveAll(moviesToSave);
        }
    }
}
