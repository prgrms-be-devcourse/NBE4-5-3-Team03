package com.example.Flicktionary.domain.series.entity;

import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Column(unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String overview;

    private Integer episodeNumber;

    private String status;

    private String posterPath;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String productionCountry;

    private String productionCompany;

    private double averageRating;

    private int ratingCount;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "series_genre",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeriesCast> casts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;
}
