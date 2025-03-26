package com.example.Flicktionary.domain.movie.repository;

import com.example.Flicktionary.domain.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query("SELECT m FROM Movie m WHERE LOWER(REPLACE(m.title, ' ', '')) LIKE CONCAT('%', :keyword, '%')")
    Page<Movie> findByTitleLike(String keyword, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.casts c " +
            "LEFT JOIN FETCH c.actor a " +
            "LEFT JOIN FETCH m.director d " +
            "WHERE m.id = :id")
    Optional<Movie> findByIdWithCastsAndDirector(Long id);

    List<Movie> findByDirectorId(Long directorId);

    @Query("SELECT m.tmdbId FROM Movie m")
    Set<Long> findAllTmdbIds();
}
