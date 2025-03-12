package com.example.Flicktionary.domain.actor.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import com.example.Flicktionary.domain.movie.repository.MovieCastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final MovieCastRepository movieCastRepository;

    // 특정 배우 조회 (출연 영화 포함)
    public Optional<Actor> getActorById(Long id) {
        return actorRepository.findById(id);
    }

    // 배우가 출연한 영화 리스트 조회
    public List<Movie> getMoviesByActorId(Long actorId) {
        List<MovieCast> movieCasts = movieCastRepository.findMoviesByActorId(actorId);
        return movieCasts.stream()
                .map(MovieCast::getMovie)
                .distinct() // 같은 영화가 중복될 경우 제거
                .collect(Collectors.toList());
    }
}
