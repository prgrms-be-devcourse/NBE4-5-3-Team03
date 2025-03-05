package com.example.Flicktionary.domain.series.entity;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plot;

    private int episode;

    private String status;

    private String imageUrl;

    @Builder.Default
    private double avgRating = 0.0;

    @Builder.Default
    private int ratingCount = 0;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String nation;

    private String company;

    @Column(nullable = false)
    private Long tmdbId;

    @ManyToMany
    @JoinTable(
            name = "series_genre",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Singular
    private List<Genre> genres;

    @ManyToMany
    @JoinTable(
            name = "series_actor",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Singular
    private List<Actor> actors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;
}
