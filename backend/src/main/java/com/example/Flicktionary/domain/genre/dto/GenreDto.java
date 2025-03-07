package com.example.Flicktionary.domain.genre.dto;

import com.example.Flicktionary.domain.genre.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class GenreDto {
    @NonNull
    private Long id;

    private final String name;

    public GenreDto(Genre genre) {
        this.id =  genre.getId();
        this.name = genre.getName();
    }
}
