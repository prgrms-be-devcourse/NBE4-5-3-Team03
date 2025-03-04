package com.example.Flicktionary.Series.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plot;

    private int episode;

    private SeriesStatus status;

    private String imageUrl;

    private double avgRating;

    private int ratingCount;

    private LocalDate releaseStartDate;

    private LocalDate releaseEndDate;

    private String nation;

    private String company;
}
