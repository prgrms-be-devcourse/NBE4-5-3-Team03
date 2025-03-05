package com.example.Flicktionary.domain.series.repository;

import com.example.Flicktionary.domain.series.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {
    Optional<Series> findByTmdbId(Long tmdbId);
}
