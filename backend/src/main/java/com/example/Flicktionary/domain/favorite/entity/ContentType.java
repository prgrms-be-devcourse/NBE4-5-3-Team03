package com.example.Flicktionary.domain.favorite.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ContentType {
    MOVIE("영화"),
    SERIES("시리즈");

    private final String description;
}
