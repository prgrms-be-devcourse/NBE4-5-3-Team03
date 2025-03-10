package com.example.Flicktionary.domain.movie.dto;

import com.example.Flicktionary.domain.actor.dto.ActorDto;
import com.example.Flicktionary.domain.movie.entity.MovieCast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieCastDto {
    private final ActorDto actor;
    private final String character;

    public MovieCastDto(MovieCast movieCast) {
        this.actor = new ActorDto(movieCast.getActor());
        this.character = movieCast.getCharacterName();
    }
}
