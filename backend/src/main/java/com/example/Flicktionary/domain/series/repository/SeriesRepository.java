package com.example.Flicktionary.domain.series.repository;

import com.example.Flicktionary.domain.series.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    @EntityGraph(attributePaths = {"director", "casts", "casts.actor"})
    Optional<Series> findById(Long id);

    Optional<Series> findByTmdbId(Long tmdbId);

    @Query("SELECT s FROM Series s WHERE s.title LIKE CONCAT('%', :keyword, '%')")
    Page<Series> findByTitleLike(@Param("keyword") String keyword, Pageable pageable);

    List<Series> findByDirectorId(Long directorId);

    @Query("SELECT s.tmdbId FROM Series s")
    Set<Long> findAllTmdbIds();
}